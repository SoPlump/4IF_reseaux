/** *
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */
package fr.reseaux.server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class EchoServerMultiThreaded {
    
    static ServerListener sl;

    /*static void SendMessage(Socket clientSocket) {
        try {
            BufferedReader socIn = null;
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            String line = socIn.readLine();
            socOut.println(line);
            System.out.println(line);
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }*/
    /**
     * main method
     *
     * @param EchoServer port
     *
     *
     */
    public static void main(String args[]) {
        ServerSocket listenSocket;

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }

        try {
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            sl = new ServerListener();
            sl.start();
            System.out.println("Server ready...");
            int i = 0;
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket);
                ct.start();
                sl.addClient(clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }
    
    static ServerListener getListener() {
        return sl;
    }
}
