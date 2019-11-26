package fr.reseaux.httpserver.server;

import java.util.HashMap;

public class Response {

    public enum ResponseCode {

        STATUS_404(404),
        STATUS_200(200),
        STATUS_500(500);

        int statusCode = 0;

        ResponseCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    private ResponseCode statusCode;

    private HashMap<String, String> responseHeader;

    private String responseBody;


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