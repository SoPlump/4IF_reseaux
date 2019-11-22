package fr.reseaux.client;

import fr.reseaux.client.view.UIController;
import fr.reseaux.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Controller {
    private static final Logger LOGGER = LogManager.getLogger(Controller.class);

    private static UIController uiController;

    private static EchoClient client;

    public Controller(String[] args) throws IOException {
        Controller.client = new EchoClient(args);
        Controller.client.start();
    }

    public void setUiController(UIController uiController) {
        Controller.uiController = uiController;
    }

    public void sendMessage(Message msg) throws IOException {
        this.client.doWrite(msg);
    }

    public static void closeApp() {
        Controller.uiController.close();
    }

    public static void printMessage() {
        Controller.uiController.printMessage(Controller.client.getMessage());
    }
}
