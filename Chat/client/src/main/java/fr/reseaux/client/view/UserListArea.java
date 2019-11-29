package fr.reseaux.client.view;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserListArea extends ScrollPane {

    private static final Logger LOGGER = LogManager.getLogger(ConversationArea.class);

    private List<Label> userItems;

    private VBox userList;

    private int i;

    public UserListArea() {

        super();

        this.userItems = new ArrayList<>();
        this.userList = new VBox();
        this.setContent(userList);
        this.userItems.add(new Label("Users on the group"));
        this.userItems.add(new Label(""));
        this.userList.getChildren().add(userItems.get(0));
        this.userList.getChildren().add(userItems.get(1));
        i = 2;
    }

    public void addUser(String username) {
        userItems.add(new Label(username));
        this.userList.getChildren().add(userItems.get(i));
        ++i;
    }

    public void clearArea() {
        userList = new VBox();
        userItems.clear();
        this.userItems.add(new Label("Users on the group"));
        this.userItems.add(new Label(""));
        this.userList.getChildren().add(userItems.get(0));
        this.userList.getChildren().add(userItems.get(1));
        this.setContent(userList);
        i = 2;
    }
}
