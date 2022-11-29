package com.scottmo.app.controllers;

import com.scottmo.app.views.VerseEditor;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.function.Supplier;

public class SongEditorController {
    private Supplier<SongService> songService = ServiceSupplier.get(SongService.class);

    @FXML
    private TextField titleInput;
    @FXML
    private VBox lyricsContainer;
    @FXML
    private TextField verseOrderInput;
    @FXML
    private ComboBox<String> verseOrderPicker;

    private Song song;

    private VerseEditor selectedVerse;

    @FXML
    private void initialize() {
        Platform.runLater(this::populateForm);
    }

    private void populateForm() {
        song = (Song) getStage().getUserData();

        if (Strings.isNotEmpty(song.getTitle())) {
            titleInput.setText(song.getTitle());
        }
        String verseOrder = song.getVerseOrder();
        if (verseOrder != null && !verseOrder.isEmpty()) {
            verseOrderInput.setText(verseOrder);
        }

        List<SongVerse> verses = song.getVerses();

        verseOrderPicker.getItems().add("Add");
        verseOrderPicker.getSelectionModel().selectFirst();
        for (SongVerse verse : verses) {
            verseOrderPicker.getItems().add(verse.getName());
        }

        for (SongVerse verse : verses) {
            createVerseInput(verse.getName(), verse.getText(), false);
        }
    }

    @FXML
    private void onAddVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        createVerseInput("new", "", true);
    }

    @FXML
    private void onEditVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        if (selectedVerse != null) {
            selectedVerse.setEditable(true);
        }
    }

    @FXML
    private void onDeleteVerse(ActionEvent event) {
        this.lyricsContainer.getChildren().remove(selectedVerse);
        selectedVerse = null;
    }

    @FXML
    private void onCancel(ActionEvent event) {
        getStage().close();
    }

    @FXML
    private void onSave(ActionEvent event) {
        // TODO validations
        song.setTitle(titleInput.getText());
        song.setVerseOrder(verseOrderInput.getText());
        song.setVerses(lyricsContainer.getChildren().stream().map(node -> {
            VerseEditor verseEditor = (VerseEditor)node;
            return new SongVerse(verseEditor.getVerseName(), verseEditor.getVerseText());
        }).toList());

        songService.get().getStore().store(song);

        getStage().close();
    }

    @FXML
    private void onAddVerseOrder(ActionEvent event) {
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
