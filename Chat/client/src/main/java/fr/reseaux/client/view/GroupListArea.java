package fr.reseaux.client.view;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GroupListArea extends ScrollPane {

    private static final Logger LOGGER = LogManager.getLogger(ConversationArea.class);

    private List<Label> groupItems;

    private VBox groupList;

    private int i;

    public GroupListArea() {

        super();

        this.groupItems = new ArrayList<>();
        this.groupList = new VBox();
        this.setContent(groupList);
        this.groupItems.add(new Label("Groups on the server"));
        this.groupItems.add(new Label(""));
        this.groupList.getChildren().add(groupItems.get(0));
        this.groupList.getChildren().add(groupItems.get(1));
        i = 2;
    }

    public void addGroup(String groupName) {
        groupItems.add(new Label(groupName));
        this.groupList.getChildren().add(groupItems.get(i));
        ++i;
    }

    public void clearArea() {
        groupList = new VBox();
        groupItems.clear();
        this.groupItems.add(new Label("Groups on the server"));
        this.groupItems.add(new Label(""));
        this.groupList.getChildren().add(groupItems.get(0));
        this.groupList.getChildren().add(groupItems.get(1));
        this.setContent(groupList);
        i = 2;
    }
}
