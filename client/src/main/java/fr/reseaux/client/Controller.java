package fr.reseaux.client;

import fr.reseaux.client.view.UIController;
import fr.reseaux.common.Message;

import java.io.IOException;

public class Controller {
    private UIController uiController;

    private EchoClient client;

    public Controller(String[] args) throws IOException {
       //this.uiController = uiController;
        this.client = new EchoClient(args);
        client.start();
    }

    public void sendMessage(Message msg) {
        this.client.doWrite(msg);
    }
}
