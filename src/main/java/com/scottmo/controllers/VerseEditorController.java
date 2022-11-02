package com.scottmo.controllers;

import com.scottmo.services.openLyrics.OpenLyrics;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerseEditorController {
    public TextField titleInput;
    public ScrollPane lyricsContainer;
    public TextField verseOrderInput;
    public ComboBox verseOrderPicker;

    private OpenLyrics song;

    public void initialize() {
        Stage stage = (Stage) titleInput.getScene().getWindow();
        song = (OpenLyrics) stage.getUserData();
    }

    protected void handleAddVerse(ActionEvent event) {
        System.out.println("test");
    }

    public void onAddVerse(ActionEvent event) {
    }

    public void onEditVerse(ActionEvent event) {
    }

    public void onDeleteVerse(ActionEvent event) {
    }

    public void onCancel(ActionEvent event) {
    }

    public void onSave(ActionEvent event) {
    }
}
