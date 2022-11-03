package com.scottmo.controllers;

import com.scottmo.services.openLyrics.OpenLyrics;
import com.scottmo.services.openLyrics.Verse;
import com.scottmo.views.VerseEditor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;

public class SongEditorController {
    public TextField titleInput;
    public VBox lyricsContainer;
    public TextField verseOrderInput;
    public ComboBox<String> verseOrderPicker;

    private OpenLyrics song;

    private VerseEditor selectedVerse;

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
        }

        List<Verse> verses = song.getVerses();

        verseOrderPicker.getItems().add("Add");
        verseOrderPicker.getSelectionModel().selectFirst();
        for (Verse verse : verses) {
            verseOrderPicker.getItems().add(verse.getName());
        }

        for (Verse verse : verses) {
            createVerseInput(verse.getName(), verse.getLinesAsString(), false);
        }
    }

    public void onAddVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        createVerseInput("new", "", true);
    }

    public void onEditVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        if (selectedVerse != null) {
            selectedVerse.setEditable(true);
        }
    }

    public void onDeleteVerse(ActionEvent event) {
        this.lyricsContainer.getChildren().remove(selectedVerse);
        selectedVerse = null;
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

    private void createVerseInput(String verseName, String verseText, boolean isEditable) {
        VerseEditor verseEditor = new VerseEditor(verseName, verseText);
        verseEditor.setOnFocus(() -> {
            selectedVerse = verseEditor;
        });
        lyricsContainer.getChildren().add(verseEditor);
        verseEditor.setEditable(isEditable);
        if (isEditable) {
            selectedVerse = verseEditor;
        }
    }
}
