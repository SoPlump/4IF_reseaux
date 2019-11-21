/** *
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */
package fr.reseaux.server;

import java.io.*;
import java.net.*;
import fr.reseaux.common.Message;

public class ClientThread
        extends Thread {

    private Socket clientSocket;

    ClientThread(Socket s) {
        this.clientSocket = s;
    }

    public void run() {
        try {
            BufferedReader socIn = null;
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            Message msg;
            while (true) {
                msg = (Message) ois.readObject();
                EchoServerMultiThreaded.getListener().addMessage(msg);
            }
        } catch (Exception e) {
            System.err.println("Error in ClientThread:" + e);
        }
    }

}
