package fr.reseaux.client;

import fr.reseaux.common.Message;
import fr.reseaux.common.ServerRequest;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    private String username;

    private Message lastMsg;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private Inet4Address multicastAddress;

    private int multicastPort;

    private MulticastSocket multicastSocket;

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
            System.out.println("Choose your user name : "); //todo : regarder si même nom qu'un autre
            //todo : rajouter connexion
            username = "bidule";//myName.nextLine();

            // creation socket ==> connexion
            this.echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            this.socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            //socOut = new PrintStream(echoSocket.getOutputStream());

            // this.ois = new ObjectInputStream(echoSocket.getInputStream());
            if (echoSocket.getInputStream() != null) {
                this.outputStream = new ObjectOutputStream(echoSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(echoSocket.getInputStream());
            }

            multicastAddress = (Inet4Address)Inet4Address.getByName("225.225.225.225");
            multicastPort = 6789;
            multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.joinGroup(multicastAddress);

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
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                Controller.printMessage();
            }
        };

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

                //Object obj = inputStream.readObject();
                //LOGGER.debug("nani");
                //LOGGER.debug("OBJECT " + obj);
                //lastMsg = (Message) inputStream.readObject();

                //LOGGER.debug("MESSAGE RECEIVED : " + lastMsg);

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
            this.outputStream.writeObject(new ServerRequest("message", "-content:{"+msg.getContent()+"}-username:{"+username+"}"));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Message getMessage() {
        return this.lastMsg;
    }
}

