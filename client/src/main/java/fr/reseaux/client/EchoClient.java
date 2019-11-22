package fr.reseaux.client;

import fr.reseaux.common.Message;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;

public class EchoClient extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(EchoClient.class);

    private String username;

    private Message lastMsg;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    Socket echoSocket = null;
    //PrintStream socOut = null;
    BufferedReader socIn = null;


    /**
     * main method accepts a connection, receives a message from client then
     * sends an echo to the client
     */
    public EchoClient(String[] args) throws IOException {
        LOGGER.info("Creating EchoClient ...");

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
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

        try {
            String line;
            while (true) {
                line = socIn.readLine();
                lastMsg = new Message(line, this.username);

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
        LOGGER.info("send message in EchoClient " + msg.toString());

        if (msg.getContent().equals("quit")) {
            Controller.closeApp(); //todo : close socket
        }

        try {
            this.outputStream.writeObject(msg);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public Message getMessage() {
        return this.lastMsg;
    }
}

