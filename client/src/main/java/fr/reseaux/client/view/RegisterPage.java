package fr.reseaux.client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class RegisterPage extends VBox {

    private TextField usernameField;

    private PasswordField passwordField;

    private PasswordField confirmationPasswordField;

    private Button registerButton;

    private UIController uiController;

    private Hyperlink loadLoginPage;

    private Label errorLabel;

    public RegisterPage(UIController uiController) {
        super();

        this.uiController = uiController;
        this.setPrefHeight(213.0);
        this.setPrefWidth(290.0);

        this.setStyle("-fx-spacing:10; -fx-padding:100; -fx-alignment:center");
        this.setAlignment(Pos.CENTER);

        this.usernameField = new TextField();
        this.passwordField = new PasswordField();
        this.confirmationPasswordField = new PasswordField();
        this.registerButton = new Button("Register now !");
        this.registerButton.setMinWidth(70.0);

        this.registerButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.registerUser(usernameField.getText(), passwordField.getText(), confirmationPasswordField.getText());
        });

        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: {
                    this.uiController.registerUser(usernameField.getText(), passwordField.getText(), confirmationPasswordField.getText());
                }
            }
        });

        this.usernameField.setPromptText("username");
        this.passwordField.setPromptText("password");
        this.confirmationPasswordField.setPromptText("confirm your password");

        this.loadLoginPage = new Hyperlink("Go back to login.");

        loadLoginPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                uiController.loadLoginPage();
            }
        });

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        this.getChildren().add(this.usernameField);
        this.getChildren().add(this.passwordField);
        this.getChildren().add(this.confirmationPasswordField);
        this.getChildren().add(this.registerButton);
        this.getChildren().add(errorLabel);
        this.getChildren().add(this.loadLoginPage);
    }

    public void printRegistrationError(String text) {
        errorLabel.setText(text);
    }
}
