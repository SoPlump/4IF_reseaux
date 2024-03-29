package fr.reseaux.client;

import fr.reseaux.client.view.UIController;
import fr.reseaux.common.Message;
import fr.reseaux.common.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Vector;

public class Controller {

    private static final Logger LOGGER = LogManager.getLogger(Controller.class);

    private static UIController uiController;

    public static Client getClient() {
        return client;
    }

    private static Client client;

    public Controller(String[] args) throws IOException {
    }

    public void setUiController(UIController uiController) {
        Controller.uiController = uiController;
    }

    public void sendMessage(Message msg) throws IOException {
        LOGGER.info("Message Controller : " + msg);
        this.client.doWrite(msg);
    }

    public static void closeApp() {
        try {
            Message leaveMessage = new Message(client.getUsername() + " vient de se déconnecter.", "server");
            client.doWrite(leaveMessage);
            Controller.client.echoSocket.close();
            Controller.uiController.close();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            Message leaveMessage = new Message(client.getUsername() + " vient de se déconnecter.", "server");
            client.doWrite(leaveMessage);
            Controller.client.echoSocket.close();
            Controller.uiController.loadServerConnectionPage();
            //System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printMessage() {
        Vector<Message> messageToPrint = client.getMessageList();
        for (Message message : messageToPrint) {
            if ("/clear".equals(message.getContent())) {
                clearArea();
            } else if ("server".equals(message.getUsername()) && (message.getContent().contains("addUser:"))) {
                Controller.addUsers(message.getContent().substring("addUser:".length()));
            } else {
                Controller.uiController.printMessage(message);
            }
        }
        client.clearMessageList();
    }

    public static void clearArea() {
        uiController.clearArea();
    }

    public static void addUsers(String username) {
        uiController.addUserToList(username);
    }

    public static void clearUsersArea() {
        uiController.clearUsersArea();
    }

    public static void clearGroupsArea() {
        uiController.clearGroupsArea();
    }

    public static void addGroup(String groupName) {
        uiController.addGroup(groupName);
    }

    public boolean connectUser(String username, String password) {
        return client.connectUser(new User(username, password));
    }

    public boolean registerUser(String username, String password) {
        return client.registerUser(new User(username, password));
    }

    public boolean connectToServer(String ipAddress, String port) {
        try {
            Controller.client = new Client(ipAddress, port);
            Controller.client.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void printStatus(String text) {
        try {
            uiController.printStatus(text);
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void printError(String text) {
        try {
            uiController.printError(text);
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void changeGroupName(String groupName) {
        uiController.setGroupName(groupName);
    }

}
