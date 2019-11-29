package fr.reseaux.httpserver.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidParameterException;
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

    public RemoteThread(Socket remoteSocket) {
        this.remoteSocket = remoteSocket;
        try {
            //this.inStream = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
            this.outStream = new PrintWriter(remoteSocket.getOutputStream());
            this.dataOutStream = new DataOutputStream(remoteSocket.getOutputStream());
            this.dataInputStream = new DataInputStream(remoteSocket.getInputStream());
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
//                    httpPutMethod();
                    break;
                case "DELETE":
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
            for (int i = 0; i < contentLength; i++) {
                //LOGGER.info(i);
                body[i] = dataInputStream.readByte();
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
            if (request.getPath().trim().equals("/")) {
                request.setPath("/index.html");
            }

            File file = new File("src/main/resources" + request.getPath());
            if (file.exists()) {
                LOGGER.debug(file.getAbsolutePath());
                BufferedReader fileStream = new BufferedReader(new FileReader(file));

                if (ImageIO.read(file) != null) {
                    response.addHeader("Content-Type: image/png");
                } else {
                    response.addHeader("Content-type: text/html");
                }
                response.addHeader("Server: Bot");
                //StringBuilder body = new StringBuilder();
                response.setResponseBody(Files.readAllBytes(file.toPath()));
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
            //LOGGER.debug(response);
            try {
                LOGGER.debug(response.getByteResponse().length);
                dataOutStream.write(response.getByteResponse(), 0, response.toString().getBytes().length);
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
                File file = new File("src/main/resources/image.jpg");
                file.createNewFile();
                response.addHeader("Content-Type: " + Files.probeContentType(file.toPath()));
                response.addHeader("Server: Bot");
                //body = request.getRequestBodyElement("image");

                //String imageBody = new String(request.getRequestBody()).split("\n\n")[1];
                LOGGER.info(Arrays.toString(request.getRequestBody()));
                //FileWriter writer = new FileWriter(new File("src/main/resources/image.jpg"));
                //writer.write(imageBody);

                Files.write(new File("src/main/resources/image.jpg").toPath(),request.getRequestBody());
                //BufferedImage bImage = ImageIO.read(new File("src/main/resources/image.jpg");
                //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                //ImageIO.write(bImage, "jpg", bos );

                body="";

                //LOGGER.debug(request.getFirstLine());
                //LOGGER.debug(request.getRequestBody());
                //LOGGER.debug(request.);
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
            response.setResponseBody(body.getBytes());

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
