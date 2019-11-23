/**
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import java.io.*;
import java.net.*;

import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import fr.reseaux.common.User;
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
            String username, password, content;
            while (true) {
                request = (ServerRequest) ois.readObject();
                switch (request.getRequestType()) {
                    case "message":
                        content = request.getRequestAttribute("content");
                        username = request.getRequestAttribute("username");
                        message = new Message(content, username);
                        Server.getMulticastThread().addMessage(message);
                        break;
                    case "login":
                        username = request.getRequestAttribute("username");
                        password = request.getRequestAttribute("password");
                        Server.getMulticastThread().connectUser(new User(username, password));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
