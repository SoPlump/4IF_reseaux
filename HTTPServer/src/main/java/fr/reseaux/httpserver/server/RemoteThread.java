package fr.reseaux.httpserver.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(RemoteThread.class);

    private Socket remoteSocket;

    public RemoteThread(Socket remoteSocket) {
        this.remoteSocket = remoteSocket;
    }

    @Override
    public void run() {
        Request request = parseRequest();
    }

    private Request parseRequest() {
        try {
            LOGGER.debug("Parsing request");
            BufferedReader in = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            String firstLine = in.readLine();
            LOGGER.info(firstLine);
            String[] splitFirstLine = firstLine.split(" ");
            String requestType = splitFirstLine[0];
            String path = splitFirstLine[1];
            String httpVersion = splitFirstLine[2];

            LOGGER.info(requestType);
            LOGGER.info(path);
            LOGGER.info(httpVersion);

            PrintWriter out = new PrintWriter(remoteSocket.getOutputStream());
            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            String str = ".";
            while (!str.equals("")) {
                str = in.readLine();
            }


            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");
            // Send the HTML page
            out.println("<h1>Welcome to the Ultra Mini-WebServer</h1>");
            out.flush();
            remoteSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
