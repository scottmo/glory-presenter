package com.scottmo.app;

import atlantafx.base.theme.PrimerLight;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.logging.AppLogger;
import com.scottmo.services.logging.AppLoggerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import static com.scottmo.config.AppContext.*;

public class App extends Application implements AppLogger {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    private final Supplier<AppLoggerService> appLoggerService = ServiceSupplier.get(AppLoggerService.class);

    public Label statusLabel;

    @Override
    public void info(String msg) {
        statusLabel.setText(msg);
    }

    @Override
    public void warn(String msg) {
        // pop up
    }

    @Override
    public void error(String msg, Throwable e) {
        // pop up + log to file
        logger.error(msg, e);
        e.printStackTrace();
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
