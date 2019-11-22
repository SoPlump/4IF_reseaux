package fr.reseaux.common;

import java.io.Serializable;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerRequest implements Serializable {

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

    private String requestType;
    private String content;

    public ServerRequest(String requestType, String content) {
        this.requestType = requestType;
        this.content = content;
    }

    public String getRequestAttribute(String attributeName) {
        Pattern attributePattern = Pattern.compile(".*-" + attributeName + ":\\{(.*?)}.*");
        Matcher attributeMatcher = attributePattern.matcher(this.content);
        if (attributeMatcher.matches()) {
            return attributeMatcher.group(1);
        }
        return null;
    }


}
