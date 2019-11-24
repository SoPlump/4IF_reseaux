/**
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import fr.reseaux.common.Message;
import fr.reseaux.common.User;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MulticastThread
        extends Thread {

    private MulticastSocket multicastSocket;

    private int multicastPort;

    private Inet4Address multicastAddress;

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    public MulticastThread(int multicastPort, Inet4Address multicastAddress) {
        try {
            this.multicastSocket = new MulticastSocket(multicastPort);
            this.multicastPort = multicastPort;
            this.multicastAddress = multicastAddress;
            this.multicastSocket.joinGroup(multicastAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Message msg;
            while (true) {
                if (!messageQueue.isEmpty()) {
                    msg = messageQueue.poll();
                    DatagramPacket datagramPacket = new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, multicastAddress, multicastPort);
                    multicastSocket.send(datagramPacket);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ClientThread:" + e);
        }
    }

    public void addMessage(Message msg) {
        this.messageQueue.add(msg);
    }

}
