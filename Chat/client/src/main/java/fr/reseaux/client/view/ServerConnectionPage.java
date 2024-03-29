package fr.reseaux.client.view;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ServerConnectionPage extends VBox {

    private TextField ipAddressField;

    private TextField portField;

    private Button connexionButton;

    private UIController uiController;

    private Label errorLabel;

    public ServerConnectionPage(UIController uiController) {
        super();

        this.uiController = uiController;
        this.setPrefHeight(213.0);
        this.setPrefWidth(290.0);

        this.setStyle("-fx-spacing:10; -fx-padding:100; -fx-alignment:center");
        this.setAlignment(Pos.CENTER);

        this.ipAddressField = new TextField("localhost");
        this.portField = new TextField("1234");
        this.connexionButton = new Button("Connect to Server");
        this.connexionButton.setMinWidth(70.0);


        this.ipAddressField.setPromptText("IP Address");
        this.portField.setPromptText("Port");

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        // handlers

        this.connexionButton.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.connectToServer(ipAddressField.getText(), portField.getText());
        });

        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: {
                    this.uiController.connectToServer(ipAddressField.getText(), portField.getText());
                }
            }
        });

        this.getChildren().add(this.ipAddressField);
        this.getChildren().add(this.portField);
        this.getChildren().add(this.connexionButton);
        this.getChildren().add(errorLabel);
    }

    public void printConnectionError() {
        errorLabel.setText("Couldn't connect to Server.");
    }
}