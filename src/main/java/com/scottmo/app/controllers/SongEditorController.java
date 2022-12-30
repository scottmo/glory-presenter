package com.scottmo.app.controllers;

import com.scottmo.app.views.TileLyricsEditor;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SongEditorController {
    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);

    private final Map<String, TileLyricsEditor> lyricsEditorMap = new HashMap<>();

    private Song song;

    public TabPane titleLyricsTabs;

    @FXML
    private TextField verseOrderInput;
    @FXML
    private ComboBox<String> verseOrderPicker;

    @FXML
    private void initialize() {
        Platform.runLater(this::populateForm);
    }

    private void populateForm() {
        song = (Song) getStage().getUserData();

        String verseOrder = song.getVerseOrder();
        if (verseOrder != null && !verseOrder.isEmpty()) {
            verseOrderInput.setText(verseOrder);
        }

        verseOrderPicker.getItems().add("Add");
        verseOrderPicker.getSelectionModel().selectFirst();
        for (SongVerse verse : song.getVerses()) {
            verseOrderPicker.getItems().add(verse.getName());
        }

        for (String locale : song.getLocales()) {
            List<Pair<String, String>> verses = song.getVerses(locale).stream()
                    .map(verse -> new Pair<>(verse.getName(), verse.getText()))
                    .toList();
            TileLyricsEditor lyricsEditor = new TileLyricsEditor(song.getTitle(locale), verses);
            lyricsEditorMap.put(locale, lyricsEditor);

            Tab lyricsTab = new Tab(locale);
            lyricsTab.setContent(lyricsEditor);
            titleLyricsTabs.getTabs().add(lyricsTab);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        getStage().close();
    }

    @FXML
    private void onSave(ActionEvent event) {
        // TODO validations
        song.setVerseOrder(verseOrderInput.getText());

        lyricsEditorMap.forEach((locale, lyricsEditor) -> {
            song.setTitle(locale, lyricsEditor.getTitle());
        });

        List<SongVerse> verses = new ArrayList<>();
        lyricsEditorMap.forEach((locale, lyricsEditor) -> {
            lyricsEditor.getVerses().forEach(verseNameAndText -> {
                verses.add(new SongVerse(locale, verseNameAndText.getKey(), verseNameAndText.getValue()));
            });
        });
        song.setVerses(verses);

        songService.get().getStore().store(song);

        getStage().close();
    }

    @FXML
    private void onAddVerseOrder(ActionEvent event) {
    }

    private Stage getStage() {
        return (Stage) verseOrderInput.getScene().getWindow();
    }

}
