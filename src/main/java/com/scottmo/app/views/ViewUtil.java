package com.scottmo.app.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class ViewUtil {
    private static ViewUtil instance;
    public static ViewUtil get() {
        if (instance == null) {
            instance = new ViewUtil();
        }
        return instance;
    }

    public Stage newModal(String title, String contentFxml, Window ownerWindow) throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(contentFxml)));
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerWindow);
        return stage;
    }

    public Window getOwnerWindow(ActionEvent event) {
        return ((Node)event.getSource()).getScene().getWindow();
    }
}
