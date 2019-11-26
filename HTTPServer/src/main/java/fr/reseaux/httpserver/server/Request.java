package fr.reseaux.httpserver.server;

import java.net.Socket;

public class Request {

    public enum RequestType {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");


        private String name = "";

        RequestType(String name) {
            this.name = name;
        }
    }

    private Socket remote;

    private RequestType requestType;

    private String requestLine;

    private String requestHeader;

    private String requestBody;


}


