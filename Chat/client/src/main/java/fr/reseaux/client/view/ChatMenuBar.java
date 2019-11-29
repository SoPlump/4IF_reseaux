package fr.reseaux.client.view;


import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;

public class ChatMenuBar extends Pane {
    private UIController uiController;

    private MenuBar menuBar;

    private Menu userMenu;

    private MenuItem disconnectItem;

    private MenuItem quitItem;

    public ChatMenuBar(UIController uiController) {
        this.uiController = uiController;

        menuBar = new MenuBar();
        userMenu = new Menu("User");
        disconnectItem = new MenuItem("Disconnect");
        quitItem = new MenuItem("Quit");

        userMenu.getItems().add(disconnectItem);
        userMenu.getItems().add(quitItem);

        menuBar.getMenus().add(userMenu);

        this.getChildren().add(menuBar);

        // handlers

        this.disconnectItem.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.disconnect();
        });

        this.quitItem.addEventHandler(ActionEvent.ACTION, actionEvent -> {
            this.uiController.quit();
        });
    }
}
