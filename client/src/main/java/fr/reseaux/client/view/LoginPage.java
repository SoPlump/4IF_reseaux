package fr.reseaux.client.view;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LoginPage extends VBox {

    private TextField usernameField;

    private PasswordField passwordField;

    private Button connexionButton;

    private UIController uiController;

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

        this.connexionButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.connectUser(usernameField.getText(), passwordField.getText());
        });

        this.usernameField.setPromptText("username");
        this.passwordField.setPromptText("password");

        this.getChildren().add(this.usernameField);
        this.getChildren().add(this.passwordField);
        this.getChildren().add(this.connexionButton);
    }

    public void printConnectionError() {
        Label errorLabel = new Label("Bad username or password.");
        errorLabel.setTextFill(Color.RED);

        this.getChildren().add(errorLabel);
    }
}
