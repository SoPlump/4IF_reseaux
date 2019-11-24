package fr.reseaux.client.view;

import com.sun.swing.internal.plaf.metal.resources.metal_es;
import fr.reseaux.client.App;
import fr.reseaux.client.Controller;
import fr.reseaux.common.Message;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * JavaFX controller used to handle all the different view components
 */
public class UIController {
    private Stage stage;

    private Controller controller;

    @FXML
    private BorderPane mainPane;

    @FXML
    private FlowPane bottomFlow;

    public TextArea getMessageArea() {
        return messageArea;
    }

    private TextArea messageArea;

    private ConversationArea conversationArea;

    private Button sendButton;

    private static final Logger LOGGER = LogManager.getLogger(UIController.class);

    private LoginPage loginPage;


    public UIController(Stage stage, Controller controller) {
        this.stage = stage;

        // Conversation page
        this.sendButton = new Button();
        this.conversationArea = new ConversationArea();
        this.messageArea = new TextArea();
        this.controller = controller;

        // Login page
        this.loginPage = new LoginPage(this);
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
            Message msg = new Message(messageArea.getText(), controller.getClient().getUsername());
            this.messageArea.clear();
            //this.conversationArea.addMessage(msg);
            try {
                this.controller.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.messageArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: {
                    LOGGER.info(messageArea.getText());
                    Message msg = new Message(messageArea.getText(), "bidule");
                    messageArea.clear();
                    //this.conversationArea.addMessage(msg);
                    try {
                        controller.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        this.mainPane.setCenter(conversationArea);
        this.bottomFlow.getChildren().add(messageArea);
        this.bottomFlow.getChildren().add(sendButton);
        //this.mainPane.setCenter(conversationArea);
        this.mainPane.setCenter(loginPage);
        //this.bottomFlow.getChildren().add(messageArea);
        //this.bottomFlow.getChildren().add(sendButton);
    }

    public void close() {
        stage.close();
    }

    public void printMessage(Message msg) {
        this.conversationArea.addMessage(msg);
    }

    public void clearArea() {
        conversationArea.clearArea();
    }
    public void loadLoginPage() {

    }

    public void loadConversationPage() {

    }

    public void connectUser(String username, String password) {
        this.controller.connectUser(username, password);
    }
}
