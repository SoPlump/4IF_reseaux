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
import java.util.Set;

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
            // Reads ServerRequest Objects and handles them by request type
            while (true) {
                request = (ServerRequest) ois.readObject();
                switch (request.getRequestType()) {
                    // Case of a message sent by the user to all other users
                    case "message":
                        content = request.getRequestAttribute("content");
                        username = request.getRequestAttribute("username");
                        message = new Message(content, username);
                        LOGGER.debug("Send a Message");
                        Server.getMulticastThreadByName(currentGroup).addMessage(message);
                        break;
                    // Request to get the history of a group
                    case "getStory":
                        LOGGER.debug("Get history");
                        List<Message> messageStory = Server.getMulticastThreadByName(currentGroup).loadStory();
                        outputStream.writeObject(messageStory);
                        break;
                    // Request of connection to a group
                    case "connectToGroup":
                        ServerResponse response;
                        LOGGER.debug("Requesting connection to group chat");
                        String groupName = request.getRequestAttribute("groupName");
                        // Every user can join Global Char
                        if ("Global Chat".equals(groupName)) {
                            LOGGER.debug("Found Global Chat");
                            String responseContent = Server.getMulticastThreadByName("Global Chat").retrieveInfos();
                            response = new ServerResponse(true, responseContent);
                            this.currentGroup = "Global Chat";
                            outputStream.writeObject(response);
                            break;
                        } else {
                            LOGGER.debug("Searching for another group than Global");
                            // Case where the group name is not associated to any group
                            if (Server.getMulticastThreadByName(groupName) == null) {
                                response = new ServerResponse(false, "Sent request to unexisting group.");
                                outputStream.writeObject(response);
                                break;
                            } else {
                                // If the connection is successful, retrieves the group IP and port
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
                    // Request sent when the user wants to connect to the server
                    case "login":
                        username = request.getRequestAttribute("username");
                        password = request.getRequestAttribute("password");
                        response = Server.connectUser(new User(username, password));
                        outputStream.writeObject(response);
                        break;
                    // Request sent when the user wants to register to the server
                    case "register":
                        username = request.getRequestAttribute("username");
                        password = request.getRequestAttribute("password");
                        response = Server.registerUser(new User(username, password));
                        outputStream.writeObject(response);
                        break;
                    // Request sent when the user wants to add another user to his current group
                    case "addUser":
                        LOGGER.debug("Requesting add of a user");
                        String user = request.getRequestAttribute("user"); // Can be used later in case we have admins
                        String userToAdd = request.getRequestAttribute("username");
                        boolean exists = Server.userExists(userToAdd);
                        if (!exists) {
                            response = new ServerResponse(false, "User " + userToAdd + " does not exist.");
                            outputStream.writeObject(response);
                            break;
                        }
                        response = Server.getMulticastThreadByName(currentGroup).addUser(userToAdd);
                        outputStream.writeObject(response);
                        break;
                    // Request sent when the user wants to create a new group
                    case "createGroup":
                        LOGGER.debug("Request to create a group");
                        groupName = request.getRequestAttribute("groupName");
                        user = request.getRequestAttribute("username");
                        response = Server.addGroup(groupName, user);
                        outputStream.writeObject(response);
                        break;
                    // Request sent when the user wants to disconnect from the server
                    case "disconnect":
                        username = request.getRequestAttribute("username");
                        boolean success = Server.disconnect(username);
                        if (success) {
                            response = new ServerResponse(true, "");
                        } else {
                            response = new ServerResponse(false, "Your disconnection request has been aborted. Please retry.");
                        }
                        outputStream.writeObject(response);
                        break;
                    // Request sent when the user wants to retrieve the users allowed to a chan
                    case "userList":
                        groupName = request.getRequestAttribute("groupName");
                        Set<String> userList = Server.getWhitelist(groupName);
                        LOGGER.debug("USER LIST : " + userList);
                        outputStream.writeObject(userList);
                        break;
                    // Request sent when the user wants to retrieve the name of all chans
                    case "groupList":
                        List<String> groupList = Server.getGroups();
                        outputStream.writeObject(groupList);
                        break;


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
