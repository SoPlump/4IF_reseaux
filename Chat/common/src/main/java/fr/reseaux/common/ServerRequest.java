package fr.reseaux.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerRegistry;

import java.io.Serializable;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Used to talk from Client to Server
// Possesses a type and a content

// Content form : -attribute1:{value1}-attribute2:{value2}...
public class ServerRequest implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(ServerRequest.class);

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
        this.content = this.content.replace("\n", "").replace("\r", "");
    }

    // Retrieves the value of an attribute
    public String getRequestAttribute(String attributeName) {

        Pattern attributePattern = Pattern.compile(".*-" + attributeName + ":\\{(.*?)}.*");
        Matcher attributeMatcher = attributePattern.matcher(this.content);
        if (attributeMatcher.matches()) {
            return attributeMatcher.group(1);
        }
        LOGGER.debug(this.content);
        return null;
    }


}
