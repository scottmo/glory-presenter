package com.scottmo.controllers;

import com.scottmo.services.openLyrics.OpenLyrics;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

public class VerseEditorController {
    public TextField titleInput;
    public ScrollPane lyricsContainer;
    public TextField verseOrderInput;
    public ComboBox<String> verseOrderPicker;

    private OpenLyrics song;

    public void initialize() {
        Platform.runLater(this::populateForm);
    }

    private void populateForm() {
        song = (OpenLyrics) getStage().getUserData();

        if (Strings.isNotEmpty(song.getProperties().getTitle())) {
            titleInput.setText(song.getProperties().getTitle());
        }
        if (!song.getProperties().getVerseOrder().isEmpty()) {
            verseOrderInput.setText(String.join(" ", song.getProperties().getVerseOrder()));
            verseOrderPicker.setItems(FXCollections.observableList(song.getVerseNames()));
        }
    }

    public void onAddVerse(ActionEvent event) {
    }

    public void onEditVerse(ActionEvent event) {
    }

    public void onDeleteVerse(ActionEvent event) {
    }

    public void onCancel(ActionEvent event) {
        getStage().close();
    }

    public void onSave(ActionEvent event) {
        song.getProperties().setTitle(titleInput.getText());
        song.getProperties().setVerseOrder(Arrays.stream(verseOrderInput.getText().split(" ")).toList());
    }

    private Stage getStage() {
        return (Stage) titleInput.getScene().getWindow();
    }
}
