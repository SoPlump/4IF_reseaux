package fr.reseaux.client.view;

import fr.reseaux.client.Controller;
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

    //@FXML
    //private MenuBar menuBar;

    private ChatMenuBar menuBar;

    private FlowPane leftFlow;

    private Label groupName;

    public TextArea getMessageArea() {
        return messageArea;
    }

    private TextArea messageArea;

    private ConversationArea conversationArea;

    private Button sendButton;

    private static final Logger LOGGER = LogManager.getLogger(UIController.class);

    private LoginPage loginPage;

    private RegisterPage registerPage;

    private StatusBar statusBar;

    private ServerConnectionPage serverConnectionPage;

    private UserListArea userListArea;

    private GroupListArea groupListArea;

    private Button refreshButton;

    private FlowPane topFlow;

    public UIController(Stage stage, Controller controller) {
        this.stage = stage;

        // Conversation page
        this.menuBar = new ChatMenuBar(this);
        this.sendButton = new Button();
        this.conversationArea = new ConversationArea();
        this.messageArea = new TextArea();
        this.controller = controller;
        this.statusBar = new StatusBar();
        this.groupName = new Label();
        this.leftFlow = new FlowPane();
        this.userListArea = new UserListArea();
        this.groupListArea = new GroupListArea();
        this.refreshButton = new Button();
        this.topFlow = new FlowPane();

        // Login page
        this.loginPage = new LoginPage(this);

        // Register page
        this.registerPage = new RegisterPage(this);

        // Server Connection page
        this.serverConnectionPage = new ServerConnectionPage(this);
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

        // Refresh button
        this.refreshButton.setText("Rafraichir");
        this.refreshButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            LOGGER.debug("CLlocalIC SUR LE BOUTON RAFRAICHIR");
            Controller.clearUsersArea();
            Controller.getClient().readUsers(Controller.getClient().getCurrentGroup());
            Controller.clearGroupsArea();
            Controller.getClient().readGroups();
        });

        this.messageArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: {
                    LOGGER.info(messageArea.getText());
                    Message msg = new Message(messageArea.getText(), controller.getClient().getUsername());
                    messageArea.clear();
                    try {
                        controller.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        topFlow.getChildren().add(menuBar);
        topFlow.getChildren().add(statusBar);

        this.userListArea.setPrefHeight(150);
        this.groupListArea.setPrefHeight(150);
        this.bottomFlow.getChildren().add(messageArea);
        this.bottomFlow.getChildren().add(sendButton);
        this.leftFlow.setPrefWidth(80.0);
        this.leftFlow.getChildren().add(groupName);
        this.leftFlow.getChildren().add(userListArea);
        this.leftFlow.getChildren().add(groupListArea);

        // Load the server connection page first

        loadServerConnectionPage();
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
        this.mainPane.setLeft(leftFlow);
        this.mainPane.setTop(topFlow);
        this.mainPane.setBottom(bottomFlow);
    }

    public void loadRegisterPage() {
        this.mainPane.setCenter(registerPage);
        LOGGER.debug("loading register page");
    }

    public void loadServerConnectionPage() {
        this.mainPane.setLeft(null);
        this.mainPane.setBottom(null);
        this.mainPane.setTop(null);
        this.mainPane.setCenter(serverConnectionPage);
        LOGGER.debug("loading server connection page");
    }

    public void connectUser(String username, String password) {
        if (this.controller.connectUser(username, password)) {
            loadConversationPage();
        } else {
            this.loginPage.printConnectionError();
        }
    }

    public void registerUser(String username, String password, String confirmationPassword) {
        LOGGER.debug("meme mdp : " + password.equals(confirmationPassword));
        if ((password.equals("")) || (username.equals(""))) {
            this.registerPage.printRegistrationError("Please enter an username and a password.");
            return;
        }
        if (password.equals(confirmationPassword)) {
            if (this.controller.registerUser(username, password)) {
                LOGGER.debug("UIController : creating a user");
                loadConversationPage();
            } else {
                this.registerPage.printRegistrationError("User already exists.");
            }
        } else {
            this.registerPage.printRegistrationError("Please enter the same password.");
        }
    }

    public void connectToServer(String ipAddress, String port) {
        if (this.controller.connectToServer(ipAddress, port)) {
            loadLoginPage();
        } else {
            this.serverConnectionPage.printConnectionError();
        }
    }

    public void printStatus(String message) {
        this.statusBar.setStatus(message);
    }

    /**
     * Displays an error message in the {@link StatusBar}
     *
     * @param error the error message to print
     */
    public void printError(String error) {
        this.statusBar.setError(error);
    }

    public void setGroupName(String groupName) {
        this.groupName.setText(groupName);
    }


    public void addUserToList(String username) {
        this.userListArea.addUser(username);
    }

    public void clearUsersArea() {
        this.userListArea.clearArea();
    }

    public void addGroup(String groupName) {
        this.groupListArea.addGroup(groupName);
    }

    public void clearGroupsArea() {
        this.groupListArea.clearArea();
    }

    public void disconnect() {
        Controller.getClient().disconnect();
    }

    public void quit() {
        Controller.getClient().close();
    }
}


