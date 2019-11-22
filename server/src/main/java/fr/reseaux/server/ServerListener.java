/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.reseaux.server;

import fr.reseaux.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author sraudrant
 */
public class ServerListener extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(ServerListener.class);

    private HashMap<String, Socket> clientList;

    //private HashMap<String, ObjectOutputStream> outputStreamMap;

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private int userId = 0;

    public ServerListener() {
        this.clientList = new HashMap<String, Socket>();
    }

    public void addClient(Socket clientSocket) throws IOException {
        this.clientList.put("Utilisateur" + userId, clientSocket);
        //this.outputStreamMap.put("Utilisateur" + userId, new ObjectOutputStream(clientSocket.getOutputStream()));
        userId++;
    }

    @Override
    public synchronized void run() {
        try {
            while (true) {
                if (!messageQueue.isEmpty()) {
                    Message messageToSend = messageQueue.poll();
                    for (String user : this.clientList.keySet()) {
                        //ObjectOutputStream outputStream = new ObjectOutputStream(clientList.get(user).getOutputStream());
                        if (user != messageToSend.getUsername()) {
                            PrintStream socOut = new PrintStream(clientList.get(user).getOutputStream());
                            socOut.println(messageToSend);
                            //outputStream.flush();
                            //outputStream.writeObject(messageToSend);
                            //this.outputStreamMap.get(user).writeObject(messageToSend);
                        }
                        //outputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            System.out.println("Problem with ServerListener Thread.");
        }

    }

    public void addMessage(Message msg) {
        this.messageQueue.add(msg);
    }
}
