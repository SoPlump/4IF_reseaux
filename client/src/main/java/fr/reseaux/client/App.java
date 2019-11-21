package fr.reseaux.client;

import fr.reseaux.client.view.UIController;
import fr.reseaux.common.Message;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class App extends Application {

    private static String[] args;

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    /**
     * Entry point called by JavaFX to launch the app
     * @param stage the main {@link Stage} of the app
     * @throws IOException if the main FXML document could not be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("Starting JavaFX application ...");

        // create fxml loader
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/scene.fxml")
        );

        // create and set controller
        Controller controller = new Controller(args);
        UIController uiController = new UIController(stage, controller);
        loader.setController(uiController);

        // load scene
        Scene scene = new Scene(loader.load());

        // init and show stage
        stage.setTitle("SendgIF");
        stage.setScene(scene);
        stage.show();

        LOGGER.info("JavaFX application successfully started");
    }

    public static void main(String[] args) throws IOException {
        // launch javafx app
        LOGGER.info(Runtime.getRuntime().maxMemory());
        fr.reseaux.client.App.args = args;
        launch(args);
    }

}
