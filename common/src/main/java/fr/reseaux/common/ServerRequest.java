package fr.reseaux.common;

import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerRequest {

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private String requestType;
    private String content;
    private Socket clientSocket;

    public ServerRequest(String requestType, String content, Socket clientSocket) {
        this.requestType = requestType;
        this.content = content;
        this.clientSocket = clientSocket;
    }

    public String getRequestAttribute(String attributeName) {
        Pattern attributePattern = Pattern.compile(".*-" + attributeName + ":{(.*)}-.*");
        Matcher attributeMatcher = attributePattern.matcher(this.content);
        if (attributeMatcher.matches()) {
            return attributeMatcher.group(1);
        }
        return null;
    }


}
