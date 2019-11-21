package fr.reseaux.client.view;

import fr.reseaux.common.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX controller used to handle all the different view components
 */
public class UIController {
    private Stage stage;

    @FXML
    private BorderPane mainPane;

    @FXML
    private FlowPane bottomFlow;

    private TextArea messageArea;

    private ConversationArea conversationArea;

    private Button sendButton;

    private static final Logger LOGGER = LogManager.getLogger(UIController.class);

    public UIController(Stage stage) {
        this.stage = stage;
        this.sendButton = new Button();
        this.conversationArea = new ConversationArea();
        this.messageArea = new TextArea();
    }

    public void initialize() {
        LOGGER.info("Initializing UI");

        // Message area
        this.messageArea.setPrefHeight(58.0);
        this.messageArea.setPrefWidth(246.0);
        this.messageArea.setPromptText("Enter your message here");

        // Send button
        this.sendButton.setText("Send");
        this.sendButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            String text = messageArea.getText();
            this.messageArea.clear();
            this.conversationArea.addMessage(new Message(text, "bidule"));
            //this.controller.computeRound();
        });

        this.mainPane.setCenter(conversationArea);
        this.bottomFlow.getChildren().add(messageArea);
        this.bottomFlow.getChildren().add(sendButton);
    }
}
