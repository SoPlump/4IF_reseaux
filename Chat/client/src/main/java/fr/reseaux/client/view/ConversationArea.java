package fr.reseaux.client.view;

import fr.reseaux.client.Controller;
import fr.reseaux.common.Message;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ConversationArea extends ScrollPane {
    private static final Logger LOGGER = LogManager.getLogger(ConversationArea.class);

    private List<Label> conversationItems;
    //todo : faire une liste
    private Label conversationItem;

    private VBox conversation;
    private int i;

    public ConversationArea() {
        super();

        this.conversationItems = new ArrayList<>();
        this.conversation = new VBox();
        this.conversationItems.add(new Label("Bienvenue dans la conversation :)"));

        this.setContent(conversation);
        this.conversation.getChildren().add(conversationItems.get(0));
        i = 1;
    }

    public void addMessage(Message msg) {
        conversationItems.add(new Label(msg.toString()));
        this.conversation.getChildren().add(conversationItems.get(i));
        ++i;
    }

    private void printMessage() {
        //this.conversation.getChildren().add(conversationItems.);
    }

    public void clearArea() {
        conversation = new VBox();
        conversationItems.clear();
        this.conversationItems.add(new Label("Bienvenue dans la conversation :)"));
        this.conversation.getChildren().add(conversationItems.get(0));
        this.setContent(conversation);
        i = 1;
    }
}
