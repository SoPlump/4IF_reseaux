package fr.reseaux.httpserver.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

public class RemoteThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(RemoteThread.class);

    private Socket remoteSocket;

    private Request request;

    private BufferedReader inStream;

    private PrintWriter outStream;

    public RemoteThread(Socket remoteSocket) {
        this.remoteSocket = remoteSocket;
        try {
            this.inStream = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            this.outStream = new PrintWriter(remoteSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (remoteSocket.isClosed()){
            return;
        }
        parseRequest();
    }

    private Request parseRequest() {
        //try {
            parseHeader();
            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP
            // headers.
            /*
            String str = ".";
            while (!str.equals("")) {
                str = inStream.readLine();
            }
            */



            // Send the response
            //out.println("HTTP/1.0 200 OK");
            //out.println("Content-Type: text/html");
            //out.println("Server: Bot");
            //// this blank line signals the end of the headers
            //out.println("");
            //// Send the HTML page
            //out.println("<h1>Welcome to the Ultra Mini-WebServer</h1>");
            //out.flush();
            // Send the headers
            //remoteSocket.close();
        /*} catch (IOException e) {
            e.printStackTrace();
        }

         */
        return null;
    }

    private void parseHeader() {
        try {
            this.request = new Request();

            // Parsing the first line

            //LOGGER.debug("Parsing request");

            String firstLine = inStream.readLine();
            if (firstLine == null) {
                throw new NullPointerException();
            }

            this.request.setFirstLine(firstLine);
            LOGGER.info(firstLine);
            String[] splitFirstLine = firstLine.split(" ");
            if (splitFirstLine.length != 3) {
                throw new InvalidParameterException();
            }
            this.request.setRequestType(splitFirstLine[0]);
            this.request.setPath(splitFirstLine[1]);

            // Parsing all the header

            String line = inStream.readLine();
            while (!line.equals("")) {
                request.addHeader(line);
                line = inStream.readLine();
            }

            LOGGER.debug("HEADER : " + request.getRequestHeader());

            // Handling request type

            //LOGGER.debug("Arrival on Switch");
            switch (splitFirstLine[0]) {
                case "GET" :
                    httpGetMethod();
                    break;
                case "POST" :
                    parseBody();
                    httpPostMethod();
                    break;
                case "PUT" :
                    break;
                case "DELETE" :
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private void parseBody() {
        try {
            int contentLength = Integer.parseInt(request.getRequestHeader().get("Content-Length"));
            StringBuilder body = new StringBuilder();

            for (int i = 0 ; i < contentLength ; ++i) {
                body.append((char)inStream.read());
            }

            request.setRequestBody(body.toString());

            LOGGER.debug("BODY : " + body.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void httpGetMethod() {
        try {
            Response response = new Response();
            //LOGGER.debug(request.getPath());

            File file = new File("src/main/resources" + request.getPath());
            //LOGGER.debug(file.getAbsolutePath());
            BufferedReader fileStream = new BufferedReader(new FileReader(file));

            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = fileStream.readLine()) != null) {
                body.append(line + "\n");
            }
            //LOGGER.debug(body.toString());
            response.setStatusCode(200);
            response.setResponseBody(body.toString().getBytes());
            //LOGGER.debug(response);
            outStream.println(response);
            outStream.flush();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void httpPostMethod() {
        try {
            Response response = new Response();

            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");

            String body = "";

            response.setStatusCode(200);
            response.setResponseBody(body.getBytes());

            outStream.println(response);
            outStream.flush();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
