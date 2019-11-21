package fr.reseaux.server;

import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Server{

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    public static void main(String[] args) {
        // launch javafx app
        LOGGER.info(Runtime.getRuntime().maxMemory());
    }
}
