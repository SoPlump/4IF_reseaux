package fr.reseaux.httpserver.server;

import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

public class Request {

    private enum RequestType {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE"),
        HEAD("HEAD");


        private String name = "";

        RequestType(String name) {
            this.name = name;
        }
    }

    private RequestType requestType;

    private String firstLine;

    private HashMap<String, String> requestHeader;

    private byte[] requestBody;

    private String path;

    public Request() {
        requestHeader = new HashMap<>();
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = RequestType.valueOf(requestType);
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public HashMap<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void addHeader(String line) {
        String[] header = line.split(": ");

        this.requestHeader.put(header[0], header[1]);
    }

    public byte[] getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestBodyElement(String element) {
        String[] responses = requestBody.toString().split("&");
        for(int i = 0; i < responses.length; ++i) {
            String[] input = responses[i].split("=");
            if (input[0].equals(element)) {
                return input[1];
            }
        }
        throw new InvalidParameterException();
    }
}


