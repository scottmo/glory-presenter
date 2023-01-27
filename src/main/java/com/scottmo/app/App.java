package com.scottmo.app;

import atlantafx.base.theme.PrimerLight;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.logging.AppLogger;
import com.scottmo.services.logging.AppLoggerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import static com.scottmo.config.AppContext.*;

public class App extends Application implements AppLogger {

    private final Logger logger = LogManager.getRootLogger();

    private final Supplier<AppLoggerService> appLoggerService = ServiceSupplier.get(AppLoggerService.class);

    public Label statusLabel;

    @Override
    public void info(String msg) {
        Alert alert = new Alert(Alert.AlertType.NONE, msg, ButtonType.CLOSE);
        alert.showAndWait();
    }

    @Override
    public void warn(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    public void error(String msg, Throwable e) {
        logger.error(msg, e);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(msg);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            error("Oops!: " + e.getMessage() + "(see error.log for details)", e);
        });
        appLoggerService.get().registerLogger(this);

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/main.fxml")));

        stage.setTitle(APP_NAME);
        stage.setScene(new Scene(root, APP_WIDTH, APP_HEIGHT));
        stage.show();
        // put window to front to avoid it to be hidden behind others.
        stage.setAlwaysOnTop(true);
        stage.requestFocus();
        stage.toFront();
        stage.setAlwaysOnTop(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
