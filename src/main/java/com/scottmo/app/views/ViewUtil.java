package com.scottmo.app.views;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
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

    public Stage getStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    public static final FileChooser.ExtensionFilter FILE_EXT_PPTX =
            new FileChooser.ExtensionFilter("Powerpoint (*.pptx)", "*.pptx");
    public static final FileChooser.ExtensionFilter FILE_EXT_OPENLYRICS =
            new FileChooser.ExtensionFilter("OpenLyrics (*.xml)", "*.xml");
    public static final FileChooser.ExtensionFilter FILE_EXT_BIBLEOSIS =
            new FileChooser.ExtensionFilter("Bible Osis Format (*.xml)", "*.xml");
    public void attachFilePickerToInput(TextField input, String initDir, FileChooser.ExtensionFilter extensionFilter) {
        input.setOnMouseClicked((MouseEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            File pptxTemplateDir = new File(initDir);
            fileChooser.setInitialDirectory(pptxTemplateDir);
            fileChooser.getExtensionFilters().add(extensionFilter);
            File selectedFile = fileChooser.showOpenDialog(getStage(input));
            // selected new template file
            if (selectedFile != null) {
                // show just the file name if file is in template folder, otherwise ues full path
                if (selectedFile.getParentFile().getAbsolutePath().equals(pptxTemplateDir.getAbsolutePath())) {
                    input.setText(selectedFile.getName());
                } else {
                    input.setText(selectedFile.getAbsolutePath());
                }
            }
        });
    }

    public void setVScrollSpeed(ScrollPane scrollPane, double step) {
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * step / 100;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });
    }
}
