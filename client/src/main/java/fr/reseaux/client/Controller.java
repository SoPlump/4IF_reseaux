package fr.reseaux.client;

import fr.reseaux.client.view.UIController;
import fr.reseaux.common.Message;
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
        Controller.client = new Client(args);
        Controller.client.start();
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
            Controller.client.echoSocket.close();
            Controller.uiController.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printMessage() {
        Vector<Message> messageToPrint = client.getMessageList();
        for (Message message : messageToPrint) {
            LOGGER.debug(message.getContent());
            if ("/clear".equals(message.getContent())) {
                clearArea();
            } else {
                Controller.uiController.printMessage(message);
            }
        }
        client.clearMessageList();
    }

    public static void clearArea() {
        uiController.clearArea();
    }

    public boolean connectUser(String username, String password) {
        return true;
    }
}
