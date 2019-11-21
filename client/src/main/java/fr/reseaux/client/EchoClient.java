package fr.reseaux.client;

import fr.reseaux.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EchoClient {

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    private static String username;

    /**
     * main method accepts a connection, receives a message from client then
     * sends an echo to the client
     *
     */
    public EchoClient(String[] args) throws IOException {
        LOGGER.info("Creating EchoClient ...");
        Socket echoSocket = null;
        PrintStream socOut = null;
        BufferedReader socIn = null;

        WriteThread writeTh = null;

        if (args.length != 2) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            System.out.println("Choose your user name : "); //todo : regarder si même nom qu'un autre
            //Scanner myName = new Scanner(System.in);
            username = "bidule";//myName.nextLine();
            // creation socket ==> connexion
            echoSocket = new Socket(args[0], Integer.parseInt(args[1]));
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintStream(echoSocket.getOutputStream());
            writeTh = new WriteThread(echoSocket, username);
            writeTh.start();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + args[0]);
            System.exit(1);
        }

        String line;
        try {
            while (true) {
                System.out.println(socIn.readLine());
                if (!writeTh.isAlive()) {
                    System.out.println("Deconnexion");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Déconnexion...");
        }
        socOut.close();
        socIn.close();
        echoSocket.close();
    }


    public void doWrite(Message msg) {

    }
}

