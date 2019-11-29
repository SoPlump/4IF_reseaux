package fr.reseaux.client.view;


import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A status view used to print message to help the user, or to print error
 * due to mishandling.
 *
 * @see ScrollPane
 * @see Label
 */
public class StatusBar extends HBox {

    private static final Logger LOGGER = LogManager.getLogger(StatusBar.class);

    private Label messageStatus;

    private Label messageError;

    private ScrollPane scrollPane;

    /**
     *Initialise two {@link Label} as the content of a {@link ScrollPane}
     *
     * Initialise the css id of the {@link Label} to handle the darkMode
     */
    public StatusBar() {
        super();
        this.messageStatus = new Label();
        this.messageError = new Label();
        this.messageError.setTextFill(Color.rgb(255, 0, 0));
        this.messageError.setId("label-error");
        this.scrollPane = new ScrollPane();

        this.getChildren().add(this.scrollPane);
        this.scrollPane.setContent(this.messageStatus);


        this.messageStatus.setText("Welcome to the SendgIF Chat System !");
        this.scrollPane.prefWidthProperty().bind(this.widthProperty());
        this.scrollPane.setMaxHeight(20);
        this.setPrefWidth(Double.MAX_VALUE);
    }

    /**
     * Print the status of the application.
     *
     * @param text to print
     */
    public void setStatus(String text) {
        this.messageStatus.setText(text);
        this.scrollPane.setContent(this.messageStatus);
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        LOGGER.debug("Status view message : {}\nCaller : {}.{}", text,
                caller.getClassName(), caller.getMethodName());
    }

    /**
     * Print the errors due to mishandling.
     *
     * @param text to print
     */
    public void setError(String text) {
        this.messageError.setText(text);
        this.scrollPane.setContent(this.messageError);
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        LOGGER.debug("Status view error : {}\nCaller : {}.{}", text,
                caller.getClassName(), caller.getMethodName());
    }
}

