package fr.reseaux.client;

import fr.reseaux.common.Message;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author sraudrant
 */
public class WriteThread
        extends Thread {
    
    private static String username;

    private Socket echoSocket;
    private BufferedReader stdIn = null;

    WriteThread(Socket s, String username) {
        this.echoSocket = s;
        this.stdIn = new BufferedReader(new InputStreamReader(System.in));
        this.username = username;
    }

    public void run() {
        try {
            PrintStream socOut = new PrintStream(echoSocket.getOutputStream());
            ObjectOutputStream oos = new ObjectOutputStream(echoSocket.getOutputStream());
            
            String content;
            while (true) {
                content = stdIn.readLine();
                Message message = new Message(content, username);
                if (message.getContent().equals("quit")) {
                    break;
                }
                oos.writeObject(message);
            }
            oos.close();
            socOut.close();
            stdIn.close();
        } catch (Exception e) {
            System.err.println("Error in WriteThread:" + e);
        }
    }

}
