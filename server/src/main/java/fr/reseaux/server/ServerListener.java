/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.reseaux.server;

import fr.reseaux.common.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

    private HashMap<String, Socket> clientList;

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private int userId = 0;

    public ServerListener() {
        this.clientList = new HashMap<String, Socket>();
    }

    public void addClient(Socket clientSocket) {
        this.clientList.put("Utilisateur" + userId, clientSocket);
        userId++;
    }

    @Override
    public synchronized void run() {
        try {
            while (true) {
                if (!messageQueue.isEmpty()) {
                    Message messageToSend = messageQueue.poll();
                    for (String user : this.clientList.keySet()) {
                        if (user != messageToSend.getUsername()) {
                            PrintStream socOut = new PrintStream(clientList.get(user).getOutputStream());
                            socOut.println(messageToSend);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Problem with ServerListener Thread.");
        }

    }

    public void addMessage(Message msg) {
        this.messageQueue.add(msg);
    }
}
