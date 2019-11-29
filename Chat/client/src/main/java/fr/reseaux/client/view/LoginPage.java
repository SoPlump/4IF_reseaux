package fr.reseaux.client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LoginPage extends VBox {

    private TextField usernameField;

    private PasswordField passwordField;

    private Button connexionButton;

    private UIController uiController;

    private Hyperlink newAccountLink;

    private Label errorLabel;

    public LoginPage(UIController uiController) {
        super();

        this.uiController = uiController;
        this.setPrefHeight(213.0);
        this.setPrefWidth(290.0);

        this.setStyle("-fx-spacing:10; -fx-padding:100; -fx-alignment:center");
        this.setAlignment(Pos.CENTER);

        this.usernameField = new TextField();
        this.passwordField = new PasswordField();
        this.connexionButton = new Button("Go !");
        this.connexionButton.setMinWidth(70.0);


        this.usernameField.setPromptText("username");
        this.passwordField.setPromptText("password");

        this.newAccountLink = new Hyperlink("Click here");

        TextFlow flow = new TextFlow(new Text("Don't have an account? "), newAccountLink);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        // handlers

        newAccountLink.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                uiController.loadRegisterPage();
            }
        });

        this.connexionButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.connectUser(usernameField.getText(), passwordField.getText());
        });

        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: {
                    this.uiController.connectUser(usernameField.getText(), passwordField.getText());
                }
            }
        });

        this.getChildren().add(this.usernameField);
        this.getChildren().add(this.passwordField);
        this.getChildren().add(this.connexionButton);
        this.getChildren().add(errorLabel);
        this.getChildren().add(flow);
    }

    public void printConnectionError() {
        errorLabel.setText("Bad username or password.");
    }
}
