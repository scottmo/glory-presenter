package com.scottmo.app.controllers;

import com.scottmo.app.views.TileLyricsEditor;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import com.scottmo.util.LocaleUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SongEditorController {
    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);

    private final Map<String, TileLyricsEditor> lyricsEditorMap = new HashMap<>();

    private Song song;

    @FXML
    private TabPane titleLyricsTabs;
    private int lastSelectedLocaleTab;

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

        setupTitleLyricsTabs();

        String verseOrder = song.getVerseOrder();
        if (verseOrder != null && !verseOrder.isEmpty()) {
            verseOrderInput.setText(verseOrder);
        }

        verseOrderPicker.getItems().add("Add");
        verseOrderPicker.getSelectionModel().selectFirst();
        for (SongVerse verse : song.getVerses()) {
            verseOrderPicker.getItems().add(verse.getName());
        }
    }

    private void setupTitleLyricsTabs() {
        // new tab button
        Tab addTranslationTab = new Tab("+");
        titleLyricsTabs.getTabs().add(addTranslationTab);
        // existing locales
        for (String locale : song.getLocales()) {
            List<Pair<String, String>> verses = song.getVerses(locale).stream()
                    .map(verse -> new Pair<>(verse.getName(), verse.getText()))
                    .toList();
            addTitleLyricsEditorTab(locale, song.getTitle(locale), verses);
        }

        // start with the first tab
        titleLyricsTabs.getSelectionModel().selectFirst();

        // needs to be added last since when it is initialized, it's the first tab
        // and selection is triggered
        addTranslationTab.setOnSelectionChanged(event -> {
            if (!addTranslationTab.isSelected()) return;

            String newLocale = addTitleLyricsEditorTab();
            if (newLocale == null) {
                titleLyricsTabs.getSelectionModel().select(lastSelectedLocaleTab);
            }
        });

        // brand new song
        if (titleLyricsTabs.getTabs().size() == 1) {
            String newLocale = addTitleLyricsEditorTab();
            if (newLocale == null) {
                getStage().close();
            }
        }
    }

    private String addTitleLyricsEditorTab() {
        String newLocale = askForNewLocale();
        if (newLocale != null) {
            newLocale = LocaleUtil.normalize(newLocale);
            addTitleLyricsEditorTab(newLocale);
        }
        return newLocale;
    }

    private void addTitleLyricsEditorTab(String locale) {
        // add existing verse names for new locales
        List<Pair<String, String>> verses = song.getVerses().stream()
                .map(verse -> new Pair<>(verse.getName(), ""))
                .toList();
        addTitleLyricsEditorTab(locale, "", verses);
        // select the new tab since we're adding a brand new one and not existing
        // previous should be the new tab
        titleLyricsTabs.getSelectionModel().selectPrevious();
    }

    private void addTitleLyricsEditorTab(String locale, String title, List<Pair<String, String>> verses) {
        TileLyricsEditor lyricsEditor = new TileLyricsEditor(title, verses);
        lyricsEditorMap.put(locale, lyricsEditor);

        int index = titleLyricsTabs.getTabs().size() - 1;
        Tab lyricsTab = new Tab();
        Label label = new Label(locale);
        lyricsTab.setGraphic(label); // using a label to capture click event later
        lyricsTab.setOnSelectionChanged(event -> lastSelectedLocaleTab = index);
        lyricsTab.setContent(lyricsEditor);
        titleLyricsTabs.getTabs().add(index, lyricsTab);

        label.setOnMouseClicked(event -> {
            // on double click, change locale
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                String newLocale = askForNewLocale();
                if (newLocale != null) {
                    String currentLocale = label.getText();
                    label.setText(newLocale);
                    lyricsEditorMap.remove(currentLocale);
                    lyricsEditorMap.put(newLocale, lyricsEditor);
                }
            }
        });
    }

    private String askForNewLocale() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter new locale");
        dialog.showAndWait();
        return dialog.getResult();
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
