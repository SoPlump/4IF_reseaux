package fr.reseaux.client.view;

import fr.reseaux.common.Message;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ConversationArea extends ScrollPane {

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
        conversationItems.add(new Label(msg.getContent()));
        this.conversation.getChildren().add(conversationItems.get(i));
        ++i;
    }

    private void printMessage() {
        //this.conversation.getChildren().add(conversationItems.);
    }
}
