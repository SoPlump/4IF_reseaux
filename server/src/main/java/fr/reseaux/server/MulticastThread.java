/**
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import fr.reseaux.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastThread
        extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(MulticastThread.class);

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
                    addMessageToStory(msg);
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

    public void addMessageToStory(Message message) {
        try {
            System.out.println(message);
            //File file = new File("files/story.txt");
            //System.out.println(file.exists());
            //System.out.println(file.canWrite());
            //PrintStream printStream = new PrintStream(file);
            BufferedWriter writer = new BufferedWriter(new FileWriter("files/story.txt", true));
            writer.append(message.toString()+'\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Message> loadStory() {
        try {
            Vector<Message> messageList = new Vector<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("files/story.txt"));
            String line;
            Pattern messagePattern = Pattern.compile("([0-9a-zA-Z]+?) : (.*)");
            Matcher messageMatcher;
            while ((line = bufferedReader.readLine())!=null) {
                System.out.println(line);
                messageMatcher = messagePattern.matcher(line);
                if (messageMatcher.matches()) {
                    System.out.println(line);
                    Message message = new Message(messageMatcher.group(2), messageMatcher.group(1));
                    messageList.add(message);
                }
            }
            System.out.println(messageList);
            return messageList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
