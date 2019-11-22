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
import java.util.Vector;

import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientThread
        extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ClientThread.class);

    private Socket clientSocket;

    ClientThread(Socket s) {
        this.clientSocket = s;
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
            while (true) {
                request = (ServerRequest) ois.readObject();
                LOGGER.info("On passe là !!!!");
                switch (request.getRequestType()) {
                    case "message":
                        String content = request.getRequestAttribute("content");
                        String username = request.getRequestAttribute("username");
                        message = new Message(content, username);
                        Server.getMulticastThread().addMessage(message);
                        LOGGER.info("CLIENT THREAD : " + message.getContent());
                        //Server.getListener().addMessage(message);
                    case "getStory":
                        LOGGER.info("CLIENT THREAD STORY ");
                        List<Message> messageStory = Server.getMulticastThread().loadStory();
                        LOGGER.info(messageStory);
                        outputStream.writeObject(messageStory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
