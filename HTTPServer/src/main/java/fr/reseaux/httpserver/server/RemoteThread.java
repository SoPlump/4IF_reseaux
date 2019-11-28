package fr.reseaux.httpserver.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.LookupOp;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.regex.Pattern;

public class RemoteThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(RemoteThread.class);

    private Socket remoteSocket;

    private Request request;

    private BufferedReader inStream;

    private PrintWriter outStream;

    private DataOutputStream dataOutStream;

    public RemoteThread(Socket remoteSocket) {
        this.remoteSocket = remoteSocket;
        try {
            this.inStream = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            this.outStream = new PrintWriter(remoteSocket.getOutputStream());
            this.dataOutStream = new DataOutputStream(remoteSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            if (remoteSocket.isClosed()) {
                return;
            }
            parseHeader();
            remoteSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                case "GET":
                    httpGetMethod();
                    break;
                case "POST":
                    parseBody();
                    httpPostMethod();
                    break;
                case "PUT":
                    httpPutMethod();
                    break;
                case "DELETE":
                    httpDeleteMethod();
                    break;
                case "HEAD":
                    httpHeadMethod();
                    break;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void parseBody() {
        try {
            int contentLength = Integer.parseInt(request.getRequestHeader().get("Content-Length"));
            StringBuilder body = new StringBuilder();

            for (int i = 0; i < contentLength; ++i) {
                body.append((char) inStream.read());
            }

            request.setRequestBody(body.toString());

            LOGGER.debug("BODY : " + body.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void httpGetMethod() {
        Response response = new Response();
        try {
            LOGGER.debug(request.getPath());
            request.setPath(request.getPath().replace("//", "/"));
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }

            File file = new File("src/main/resources" + request.getPath());
            if (file.exists()) {
                LOGGER.debug(file.getAbsolutePath());
                BufferedReader fileStream = new BufferedReader(new FileReader(file));

                response.addHeader("Content-Type: " + Files.probeContentType(file.toPath()));
                response.addHeader("Server: Bot");
                //StringBuilder body = new StringBuilder();
                response.setResponseBody(Files.readAllBytes(file.toPath()));
                response.setStatusCode(200);
                /*String line;
                while ((line = fileStream.readLine()) != null) {
                    body.append(line + "\n");
                }
                LOGGER.debug(body.toString());
                response.setStatusCode(200);
                response.setResponseBody(body.toString().getBytes());*/
            } else {
                response.setStatusCode(404);
                response.setResponseBody(("<h1>404 Not Found</h1>").getBytes());
            }

        } catch (IOException e) {
            response.setStatusCode(404);
            response.setResponseBody(("<h1>404 Not Found</h1>").getBytes());
        } catch (
                Exception e) {
            response.setStatusCode(500);
            response.setResponseBody(("<h1>500 Internal Server Error</h1>").getBytes());
        } finally {
            try {
                response.addHeader("Content-length: " + response.getResponseBody().length);
                LOGGER.debug(response.toString());
                dataOutStream.write(response.getByteResponse(), 0, response.getByteResponse().length);
                dataOutStream.flush();
                dataOutStream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
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

    private void httpDeleteMethod() {
        try {
            Response response = new Response();

            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");

            String body = "";

            LOGGER.debug(request.getPath());
            request.setPath(request.getPath().replace("//", "/"));
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }

            LOGGER.debug(request.getPath());
            File file = new File("src/main/resources" + request.getPath());
            LOGGER.debug(file.exists());
            LOGGER.debug(file.getAbsolutePath());
            if (file.exists()) {
                if ("/index.html".equals(request.getPath())) {
                    response.setStatusCode(500);
                    body = "<h1>Cannot change index page</h1>";
                } else if (file.delete()) {
                    response.setStatusCode(200);
                    body = "<h1>File deleted</h1>";
                } else {
                    response.setStatusCode(500);
                    body = "<h1>Couldn't delete file</h1>";
                }
            } else {
                response.setStatusCode(404);
                body = "<h1>File not Found</h1>";
            }
            response.setResponseBody(body.getBytes());

            dataOutStream.write(response.getByteResponse(), 0, response.getByteResponse().length);
            dataOutStream.flush();
            dataOutStream.close();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void httpHeadMethod() {
        Response response = new Response();
        try {
            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");

            String body = "";

            LOGGER.debug(request.getPath());
            request.setPath(request.getPath().replace("//", "/"));
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }

            File file = new File("src/main/resources" + request.getPath());
            if (file.exists()) {
                LOGGER.debug(file.getAbsolutePath());
                BufferedReader fileStream = new BufferedReader(new FileReader(file));


                //StringBuilder body = new StringBuilder();
                response.addHeader("Content-length: " + Files.readAllBytes(file.toPath()).length);
                response.setStatusCode(200);
                /*String line;
                while ((line = fileStream.readLine()) != null) {
                    body.append(line + "\n");
                }
                LOGGER.debug(body.toString());
                response.setStatusCode(200);
                response.setResponseBody(body.toString().getBytes());*/
            } else {
                response.setStatusCode(404);
                response.setResponseBody(("<h1>404 Not Found</h1>").getBytes());
            }

        } catch (IOException e) {
            response.setStatusCode(404);
            response.setResponseBody(("<h1>404 Not Found</h1>").getBytes());
        } catch (
                Exception e) {
            response.setStatusCode(500);
            response.setResponseBody(("<h1>500 Internal Server Error</h1>").getBytes());
        } finally {
            try {
                LOGGER.debug(response.toString());
                dataOutStream.write(response.toString().getBytes(), 0, response.toString().getBytes().length);
                dataOutStream.flush();
                dataOutStream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    private void httpPutMethod() {
        Response response = new Response();
        try {

            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");

            String body = "";

            parseBody();
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }
            if ("/index.html".equals(request.getPath())) {
                response.setStatusCode(500);
                body = "<h1>Cannot change index page</h1>";
            } else {

                String[] pathToFile = request.getPath().split("/");
                int i;
                String actualPath = "src/main/resources";
                File actualDir;
                if(pathToFile.length > 1) {
                    for (i = 0; i < pathToFile.length - 1; i++) {
                        actualPath = actualPath + "/" + pathToFile[i];
                        actualDir = new File(actualPath);
                        if (!actualDir.isDirectory()) {
                            actualDir.mkdir();
                        }
                    }
                }
                FileWriter writer = new FileWriter(new File("src/main/resources" + request.getPath()));
                if(request.getRequestHeader().get("Content-Type").contains("text")) { // Simple texte
                    writer.append(request.getRequestBody());
                    writer.close();
                    response.setStatusCode(200);
                    response.setResponseBody(("<h1>File created</h1>").getBytes());
                }
            }

        } catch (IOException e) {
            response.setStatusCode(404);
            response.setResponseBody(("<h1>500 Internal Server Error</h1>").getBytes());
        } catch (
                Exception e) {
            response.setStatusCode(500);
            response.setResponseBody(("<h1>500 Internal Server Error</h1>").getBytes());
        } finally {
            try {
                LOGGER.debug(response.toString());
                dataOutStream.write(response.toString().getBytes(), 0, response.toString().getBytes().length);
                dataOutStream.flush();
                dataOutStream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
/*
    public void httpPutMethod() {
        Response response = new Response();
        try {
            LOGGER.debug(request.getPath());
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }

            File file =
        } catch () {

        }

    }
*/
}
