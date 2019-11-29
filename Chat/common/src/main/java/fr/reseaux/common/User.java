package fr.reseaux.common;

import java.net.Socket;
import java.util.Objects;

// Class that is used to represent a client's account and its status at any instant on the server
public class User {

    private String username;

    private String password;

    private boolean isConnected;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isConnected = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isConnected == user.isConnected &&
                Objects.equals(username, user.username) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, isConnected);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
