package fr.reseaux.common;

import java.io.Serializable;

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

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }
    
    public String toString() {
        return this.username + " : " + this.content;
    }
}
