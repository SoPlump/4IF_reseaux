package fr.reseaux.client;

import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import fr.reseaux.common.ServerResponse;
import fr.reseaux.common.User;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.compiler.lir.gen.LIRGenerator_OptionDescriptors;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

public class Client extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private String username;

    private Message lastMsg;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Inet4Address multicastAddress;

    private int multicastPort;

    private Vector<Message> messageList = new Vector<>();

    private MulticastSocket multicastSocket;

    private Inet4Address currentAddress = null;

    private boolean noGroupJoined = true;

    Runnable updater;

    Socket echoSocket = null;
    //PrintStream socOut = null;
    BufferedReader socIn = null;


    /**
     * main method accepts a connection, receives a message from client then
     * sends an echo to the client
     */
    public Client(String[] args) throws IOException {
        LOGGER.info("Creating Client ...");

        if (args.length != 2) {
            System.out.println("Usage: java Client <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            // creation socket ==> connexion
            this.echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            this.socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));

            if (echoSocket.getInputStream() != null) {
                this.outputStream = new ObjectOutputStream(echoSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(echoSocket.getInputStream());
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + args[0]);
            System.exit(1);
        }
        LOGGER.info("vi");
    }

    public void run() {
        updater = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("Printed");
                Controller.printMessage();
            }
        };
/*
        Runnable clearer = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("Cleared");
                Controller.clearArea();
            }
        };

 */

        joinGroup("Global Chat");
        //joinGroup("Secondary Chat", updater);
        //joinGroup("Third Chat", updater);
        String line;
        byte[] buffer = new byte[1000];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            String content = "abcd";
            while (true) {
                multicastSocket.receive(datagramPacket);
                content = new String(
                        datagramPacket.getData(),
                        datagramPacket.getOffset(),
                        datagramPacket.getLength(),
                        StandardCharsets.UTF_8 // or some other charset
                );
                lastMsg = new Message(content, "bullshit"); //todo : recuperer usename
                messageList.add(lastMsg);

                // update the UI
                Platform.runLater(updater);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }

        try {
            LOGGER.info("CLOSING ALL");
            //socOut.close();
            socIn.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doWrite(Message msg) throws IOException {
        LOGGER.info("send message in Client " + msg.toString());

        if (msg.getContent().equals("quit")) {
            Controller.closeApp(); //todo : close socket
        }

        try {
            String newline = System.getProperty("line.separator");
            LOGGER.info(msg.getContent().contains(newline));
            this.outputStream.writeObject(new ServerRequest("message", "-content:{" + msg.getContent() + "}-username:{" + username + "}"));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Message getMessage() {
        LOGGER.info("Last Message : " + this.lastMsg);
        return this.lastMsg;
    }

    public void clearMessageList() {
        messageList.clear();
    }

    public Vector<Message> getMessageList() {
        return this.messageList;
    }

    public void joinGroup(String groupName) {
        try {
            LOGGER.debug("Connection requested to " + groupName);
            ServerRequest connectRequest = new ServerRequest("connectToGroup", "-username:{"+username+"}-groupName:{"+groupName+"}");
            outputStream.writeObject(connectRequest);
            ServerResponse response = (ServerResponse)this.inputStream.readObject();
            if (response.isSuccess()) {
                String address = response.getRequestAttribute("groupAddress").replace("/","");
                LOGGER.info("Adresse : " + address);
                String port = response.getRequestAttribute("groupPort");
                LOGGER.info("Port : " + port);

                multicastPort = Integer.parseInt(port);
                multicastAddress = (Inet4Address)Inet4Address.getByName(address);

                if (currentAddress != null) {
                    multicastSocket.leaveGroup(currentAddress);
                } else {
                    multicastSocket = new MulticastSocket(multicastPort);
                }
                currentAddress = multicastAddress;
                multicastSocket.joinGroup(multicastAddress);

                messageList.add(new Message("Clear the board <server_action>", "server"));
                LOGGER.info("Sending a request to get the story");
                lastMsg = new Message("Connected as " + username, "server");
                messageList.add(lastMsg);

                Platform.runLater(updater);
                ServerRequest storyRequest = new ServerRequest("getStory", "");
                this.outputStream.writeObject(storyRequest);

                //while(true) {
                Vector<Message> storyList = (Vector<Message>) this.inputStream.readObject();
                if (storyList.size() != 0) {
                    for (Message message : storyList) {
                        this.lastMsg = new Message(message.toString(), message.getUsername());
                        LOGGER.info("Last Message Is : " + lastMsg);
                        messageList.add(lastMsg);
                    }
                    Platform.runLater(updater);
                    //  break;
                }
                if (noGroupJoined) noGroupJoined = false;
                // }
            } else {
                LOGGER.debug(response.getContent());
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean connectUser(User user) {
        try {
            LOGGER.info("Trying to connect user");
            ServerRequest request = new ServerRequest("login", "-username:{"+user.getUsername()+"}-password:{"+user.getPassword()+"}");
            outputStream.writeObject(request);
            ServerResponse response = (ServerResponse) this.inputStream.readObject();

            if (response.isSuccess()) {
                this.username = user.getUsername();
                joinGroup("Global Chat");
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug(e);
        }
        return false;
    }
}

