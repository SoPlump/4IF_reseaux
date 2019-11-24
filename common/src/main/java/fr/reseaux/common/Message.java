package fr.reseaux.common;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sraudrant
 */
public class Message implements Serializable{

    private String content;
    
    private String username;

    public Message(String content, String username) {
        this.content = content;
        this.username = username;
    }

    public Message(String message) {
        message = message.replace("\n","").replace("\r", "");
        Pattern messagePattern = Pattern.compile("([a-zA-Z0-9]+?) : (.*)");
        Matcher messageMatcher = messagePattern.matcher(message);
        if (messageMatcher.matches()) {
            this.username = messageMatcher.group(1);
            this.content = messageMatcher.group(2);
        } else {
            this.username ="";
            this.content = "";
        }
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }
    
    public String toString() {
        return this.username + " : " + this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
