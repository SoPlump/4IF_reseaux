package fr.reseaux.httpserver.server;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class Response {

    private HashMap<Integer, String> statusCodes;

    private HashMap<String, String> responseHeader;

    private byte[] responseBody;

    private int statusCode;

    public Response() {
        this.responseHeader = new HashMap<>();
        this.statusCodes = new HashMap<>();
        statusCodes.put(200, "OK");
        statusCodes.put(404, "NOT FOUND");
        statusCodes.put(500, "INTERNAL SERVER ERROR");
    }

    public HashMap<String, String> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(HashMap<String, String> responseHeader) {
        this.responseHeader = responseHeader;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public void addHeader(String line) {
        String []header = line.split(": ");
        this.responseHeader.put(header[0], header[1]);
    }

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.0 " + this.statusCode+ " " + this.statusCodes.get(this.statusCode) + "\n");
        for (String key : responseHeader.keySet()) {
            response.append(key + ": " +responseHeader.get(key) +"\n");
        }
        response.append("\n");
        response.append(new String(responseBody));
        return response.toString();
    }
}


// Send the response
//out.println("HTTP/1.0 200 OK");
//out.println("Content-Type: text/html");
//out.println("Server: Bot");
//// this blank line signals the end of the headers
//out.println("");
//// Send the HTML page
//out.println("<h1>Welcome to the Ultra Mini-WebServer</h1>");
//out.flush();