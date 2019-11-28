package fr.reseaux.httpserver.server;

import java.net.Socket;
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

    private String requestBody;

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

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}


