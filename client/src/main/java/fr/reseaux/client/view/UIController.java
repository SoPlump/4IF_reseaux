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

    private RegisterPage registerPage;

    public UIController(Stage stage, Controller controller) {
        this.stage = stage;

        // Conversation page
        this.sendButton = new Button();
        this.conversationArea = new ConversationArea();
        this.messageArea = new TextArea();
        this.controller = controller;

        // Login page
        this.loginPage = new LoginPage(this);

        // Register page
        this.registerPage = new RegisterPage(this);
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
                    Message msg = new Message(messageArea.getText(), controller.getClient().getUsername());
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



        loadLoginPage();
        //loadRegisterPage();
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
        this.mainPane.setCenter(loginPage);
    }

    private void loadConversationPage() {
        this.mainPane.setCenter(conversationArea);
        this.bottomFlow.getChildren().add(messageArea);
        this.bottomFlow.getChildren().add(sendButton);
    }

    public void loadRegisterPage() {
        this.mainPane.setCenter(registerPage);
        LOGGER.debug("loading register page");
    }

    public void connectUser(String username, String password) {
        if (this.controller.connectUser(username, password)) {
            loadConversationPage();
        }
        else {
            this.loginPage.printConnectionError(); //todo: afficher la bonne erreur : déjà connecté ou mauvais mdp ou identifiant
        }
    }

    public void registerUser(String username, String password, String confirmationPassword) {
        LOGGER.debug("meme mdp : " + password.equals(confirmationPassword));
        if ((password.equals(""))||(username.equals(""))) {
            this.registerPage.printRegistrationError("Please enter an username and a password.");
            return;
        }
        if (password.equals(confirmationPassword)) {
            if (this.controller.registerUser(username, password)) {
                LOGGER.debug("UIController : creating a user");
                loadConversationPage();
            }
            else {
                this.registerPage.printRegistrationError("User already exists.");
            }
        }
        else {
            this.registerPage.printRegistrationError("Please enter the same password.");
        }
    }
}
