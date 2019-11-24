/**
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import java.io.*;
import java.net.*;
import java.util.List;
import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import fr.reseaux.common.ServerResponse;
import fr.reseaux.common.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientThread
        extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientThread.class);

    private Socket clientSocket;

    private String currentGroup;

    ClientThread(Socket s) {
        this.clientSocket = s;
        this.currentGroup = "Global Chat";
    }

    public void run() {
        try {
            BufferedReader socIn = null;
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            //PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            Message message;
            ServerRequest request;
            String username, password, content;
            while (true) {
                request = (ServerRequest) ois.readObject();
                switch (request.getRequestType()) {
                    case "message":
                        content = request.getRequestAttribute("content");
                        username = request.getRequestAttribute("username");
                        message = new Message(content, username);
                        LOGGER.debug("Send a Message");
                        Server.getMulticastThreadByName(currentGroup).addMessage(message);
                        break;
                    //Server.getListener().addMessage(message);
                    case "getStory":
                        LOGGER.debug("Get history");
                        List<Message> messageStory = Server.getMulticastThreadByName(currentGroup).loadStory();
                        outputStream.writeObject(messageStory);
                        break;
                    case "connectToGroup":
                        ServerResponse response;
                        LOGGER.debug("Requesting connection to group chat");
                        String groupName = request.getRequestAttribute("groupName");
                        if ("Global Chat".equals(groupName)) {
                            LOGGER.debug("Found Global Chat");
                            String responseContent = Server.getMulticastThreadByName("Global Chat").retrieveInfos();
                            response = new ServerResponse(true, responseContent);
                            this.currentGroup = "Global Chat";
                            outputStream.writeObject(response);
                            break;
                        } else {
                            LOGGER.debug("Searching for another group than Global");
                            if (Server.getMulticastThreadByName(groupName) == null) {
                                response = new ServerResponse(false, "Sent request to unexisting group.");
                                outputStream.writeObject(response);
                                break;
                            } else {
                                String userRequested = request.getRequestAttribute("username");
                                LOGGER.debug("Asking for connection");
                                boolean connectionAuthorized = Server.getMulticastThreadByName(groupName).accept(userRequested);
                                if (connectionAuthorized) {
                                    LOGGER.debug("Asking for informations");
                                    response = new ServerResponse(true, Server.getMulticastThreadByName(groupName).retrieveInfos());
                                    this.currentGroup = groupName;
                                    outputStream.writeObject(response);
                                    break;
                                } else {
                                    response = new ServerResponse(false, "You are not allowed to join this chan.");
                                    outputStream.writeObject(response);
                                    break;

                                }

                            }
                        }
                    case "login":
                        username = request.getRequestAttribute("username");
                        password = request.getRequestAttribute("password");
                        Server.connectUser(new User(username, password));
                        break;
                    case "addUser":
                        LOGGER.debug("Requesting add of a user");
                        String user = request.getRequestAttribute("user"); // Can be used later in case we have admins
                        String userToAdd = request.getRequestAttribute("username");
                        boolean success = Server.getMulticastThreadByName(currentGroup).addUser(userToAdd);
                        String responseContent;
                        if(success) {
                            responseContent = "Successfully added user " + userToAdd + " to group.";
                        } else {
                            responseContent = "User " + userToAdd + " was already in group or couldn't be added.";
                        }
                        response = new ServerResponse(success, responseContent);
                        outputStream.writeObject(response);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
