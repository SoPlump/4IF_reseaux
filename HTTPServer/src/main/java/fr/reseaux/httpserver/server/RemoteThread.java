package fr.reseaux.httpserver.server;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.regex.Pattern;
import java.util.Arrays;

public class RemoteThread extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(RemoteThread.class);

    private Socket remoteSocket;

    private Request request;

    private BufferedReader inStream;

    private PrintWriter outStream;

    private DataOutputStream dataOutStream;

    private DataInputStream dataInputStream;

    private static int idUser;
    private static int idImage;

    public RemoteThread(Socket remoteSocket) {
        this.remoteSocket = remoteSocket;
        try {
            //this.inStream = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            this.outStream = new PrintWriter(remoteSocket.getOutputStream());
            this.dataOutStream = new DataOutputStream(remoteSocket.getOutputStream());
            this.dataInputStream = new DataInputStream(remoteSocket.getInputStream());
            //idImage = 0;
            //idUser = 0;
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

            //String firstLine = inStream.readLine();

            boolean headersFound = false;
            byte[] tempHeads = new byte[1024];
            int i = 0;
            LOGGER.info(i);
            int combo = 0;
            String headersString = "";
            while(!headersFound && i < 1024) {
                tempHeads[i] = dataInputStream.readByte();
                LOGGER.info(i);
                if((combo%2 == 0 && tempHeads[i] == 13) || (combo%2 == 1 && tempHeads[i] == 10)) {
                    combo = combo + 1;
                } else {
                    combo = 0;
                }
                i++;
                if (combo == 4) {
                    byte [] nonNullHeader = new byte[i];
                    System.arraycopy(tempHeads, 0, nonNullHeader, 0, i);
                    headersString = new String(nonNullHeader);
                    headersFound = true;
                }
            }

            String[] headers = headersString.replace("\r\n", "\n").split("\n");
            LOGGER.debug("HEADERS : " + headersString);

            if (headers.length == 0) {
                throw new NullPointerException();
            }

            LOGGER.debug("BYTES : "+ Arrays.toString(tempHeads));

            //10 13 10 13

            this.request.setFirstLine(headers[0]);
            LOGGER.info(headers[0]);
            String[] splitFirstLine = headers[0].split(" ");
            if (splitFirstLine.length != 3) {
                throw new InvalidParameterException();
            }
            this.request.setRequestType(splitFirstLine[0]);
            this.request.setPath(splitFirstLine[1]);

            // Parsing all the header

            //String line = inStream.readLine();
            /*while (!line.equals("")) {
                request.addHeader(line);
                line = inStream.readLine();
            }*/
            for (int j = 1; j < headers.length; j++) {
                request.addHeader(headers[j]);
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
            //DataInputStream inputStream = new DataInputStream(remoteSocket.getInputStream());
            int contentLength = Integer.parseInt(request.getRequestHeader().get("Content-Length"));
            //StringBuilder body = new StringBuilder();

            byte[] body = new byte[contentLength];
            //int j = 0;
            /*
            for (int i = 0; i < contentLength; ++i) {
                body.append((char) inStream.read());
            }

             */
            int j = 0;
            for (int i = 0; i < contentLength; i++) {
                body[i] = dataInputStream.readByte();
                //LOGGER.info(i);
                /*try {
                    body2[i] = dataInputStream.readByte();
                    LOGGER.info(i);
                    j++;
                } catch (EOFException e) {
                    LOGGER.info("Length : " + request.getRequestHeader().get("Content-Length"));
                    LOGGER.info("j : " + j);
                    break;
                }
                */

            }

            request.setRequestBody(body);

            //LOGGER.debug("BODY : " + body.toString());
            //LOGGER.warn("BODY : " + Arrays.toString(body.toString().getBytes()));

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
        Response response = new Response();
        try {

            String path = request.getPath();
            String body = null;

            if("/downloadFile".equals(path)) {
                File file = new File("src/main/resources/image"+idImage+".jpg");
                LOGGER.debug(file.getAbsolutePath());
                file.createNewFile();
                response.addHeader("Content-Type: " + Files.probeContentType(file.toPath()));
                response.addHeader("Server: Bot");
                //body = request.getRequestBodyElement("image");
                ++idImage;

                //String imageBody = new String(request.getRequestBody()).split("\n\n")[1];
                LOGGER.info(Arrays.toString(request.getRequestBody()));
                //FileWriter writer = new FileWriter(new File("src/main/resources/image.jpg"));
                //writer.write(imageBody);

                int combo = 0;
                boolean firstLineFound = false;
                byte tempByte;
                int index = 0;
                byte [] currentBody = request.getRequestBody();
                while(!firstLineFound) {
                    tempByte = currentBody[index];
                    index ++;
                    if((combo%2 == 0 && tempByte == 13) || (combo%2 == 1 && tempByte == 10)) {
                        combo = combo + 1;
                    } else {
                        combo = 0;
                    }
                    if (combo == 4) {
                        firstLineFound = true;
                    }
                }
                int newBodyLength = Integer.parseInt(request.getRequestHeader().get("Content-Length")) - index;
                LOGGER.info(index);
                LOGGER.info(newBodyLength);
                byte[] imageBody = new byte[newBodyLength];

                System.arraycopy(currentBody, index, imageBody, 0, newBodyLength);




                Files.write(file.toPath(),imageBody);
                body="";
                response.setResponseBody(imageBody);
            }
            else if ("/createUser".equals(path)) {
                File file = new File("src/main/resources/users" + idUser + ".txt");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.append(request.getRequestBodyElement("username") + "\n" + request.getRequestBodyElement("password"));
                writer.close();
                ++idUser;

                response.addHeader("Content-Type: text/html");
                response.addHeader("Server: Bot");
                body = "<h1> Bienvenue " + request.getRequestBodyElement("username") + "</h1>";
            }

            response.setStatusCode(200);
            //response.setResponseBody(body.getBytes());

            dataOutStream.write(response.getByteResponse(), 0, response.getByteResponse().length);
            dataOutStream.flush();
            dataOutStream.close();
            //outStream.println(response);
            //outStream.flush();

        } catch (Exception e) {
            response.setStatusCode(500);
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void httpDeleteMethod() {
        Response response = new Response();
        String body = "";
        try {

            response.addHeader("Content-Type: text/html");
            response.addHeader("Server: Bot");

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

        } catch (Exception e) {
            response.setStatusCode(500);
            body = "<h1>Couldn't delete file</h1>";
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {

                //response.setStatusCode(200);
                response.setResponseBody(body.getBytes());

                dataOutStream.write(response.getByteResponse(), 0, response.getByteResponse().length);
                dataOutStream.flush();
                dataOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                if(request.getRequestHeader().get("Content-Type").contains("text")) { // Simple texte
                    FileWriter writer = new FileWriter(new File("src/main/resources" + request.getPath()));
                    writer.append(request.getRequestBody().toString());
                    writer.close();
                    response.setStatusCode(200);
                    response.setResponseBody(("<h1>File created</h1>").getBytes());
                } else if(request.getRequestHeader().get("Content-Type").contains("multipart")) { // Image
                    File file = new File("src/main/resources" + request.getPath());
                    LOGGER.debug(file.getAbsolutePath());
                    file.createNewFile();
                    //response.addHeader("Content-Type: " + Files.probeContentType(file.toPath()));
                    //response.addHeader("Server: Bot");
                    //body = request.getRequestBodyElement("image");

                    //String imageBody = new String(request.getRequestBody()).split("\n\n")[1];
                    //LOGGER.info(Arrays.toString(request.getRequestBody()));
                    //FileWriter writer = new FileWriter(new File("src/main/resources/image.jpg"));
                    //writer.write(imageBody);

                    int combo = 0;
                    boolean firstLineFound = false;
                    byte tempByte;
                    int index = 0;
                    byte [] currentBody = request.getRequestBody();
                    while(!firstLineFound) {
                        tempByte = currentBody[index];
                        index ++;
                        if((combo%2 == 0 && tempByte == 13) || (combo%2 == 1 && tempByte == 10)) {
                            combo = combo + 1;
                        } else {
                            combo = 0;
                        }
                        if (combo == 4) {
                            firstLineFound = true;
                        }
                    }
                    int newBodyLength = Integer.parseInt(request.getRequestHeader().get("Content-Length")) - index;
                    LOGGER.info(index);
                    LOGGER.info(newBodyLength);
                    byte[] imageBody = new byte[newBodyLength];

                    System.arraycopy(currentBody, index, imageBody, 0, newBodyLength);




                    Files.write(file.toPath(),imageBody);
                    //body="";
                    //response.setResponseBody(imageBody);
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
                dataOutStream.write(response.getByteResponse(), 0, response.getByteResponse().length);
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
