package com.scottmo.app.controllers;

import com.scottmo.app.Labels;
import com.scottmo.app.views.TileLyricsEditor;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import com.scottmo.util.LocaleUtil;
import com.scottmo.util.StringUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
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

    private int songId;

    public TabPane titleLyricsTabs;
    private int lastSelectedLocaleTab;

    public TextField authorsInput;
    public TextField songbookInput;
    public TextField songbookEntryInput;
    public TextField verseOrderInput;
    public MenuButton verseOrderPicker;
    public TextField copyrightInput;
    public TextArea commentsInput;
    public TextField publisherInput;

    public void initialize() {
        Platform.runLater(this::populateForm);
    }

    private void populateForm() {
        Song song = (Song) getStage().getUserData();

        songId = song.getId();

        setupTitleLyricsTabs(song);

        String verseOrder = song.getVerseOrder();
        if (verseOrder != null && !verseOrder.isEmpty()) {
            verseOrderInput.setText(verseOrder);
        }

        refreshVerseOrderList();
        // TODO: optimize this instead of re-init every click
        verseOrderPicker.setOnMousePressed(event -> {
            refreshVerseOrderList();
        });

        authorsInput.setText(String.join(", ", song.getAuthors()));
        songbookInput.setText(song.getSongBook());
        songbookEntryInput.setText(song.getEntry());
        copyrightInput.setText(song.getCopyright());
        commentsInput.setText(song.getComments());
        publisherInput.setText(song.getPublisher());
    }

    private void refreshVerseOrderList() {
        var verseOptions = verseOrderPicker.getItems();
        verseOptions.clear();

        getVerseNames().forEach(verseName -> {
            MenuItem item = new MenuItem(verseName);
            item.setOnAction(this::onAddVerseOrder);
            verseOptions.add(item);
        });
    }

    private List<String> getVerseNames() {
        List<String> verseNames = new ArrayList<>();
        lyricsEditorMap.forEach((locale, lyricsEditor) -> {
            lyricsEditor.getVerses().forEach(verseNameAndText -> {
                String verseName = verseNameAndText.getKey();
                if (!verseNames.contains(verseName)) {
                    verseNames.add(verseName);
                }
            });
        });
        return verseNames;
    }

    private void setupTitleLyricsTabs(Song song) {
        // new tab button
        Tab addTranslationTab = new Tab(Labels.TAB_ADD_LOCALE);
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

            String newLocale = addTitleLyricsEditorTab(song);
            if (newLocale == null) {
                titleLyricsTabs.getSelectionModel().select(lastSelectedLocaleTab);
            }
        });

        // brand new song
        if (titleLyricsTabs.getTabs().size() == 1) {
            String newLocale = addTitleLyricsEditorTab(song);
            if (newLocale == null) {
                getStage().close();
            }
        }
    }

    private String addTitleLyricsEditorTab(Song song) {
        String newLocale = askForNewLocale();
        if (newLocale != null) {
            newLocale = LocaleUtil.normalize(newLocale);
            addTitleLyricsEditorTab(song, newLocale);
        }
        return newLocale;
    }

    private void addTitleLyricsEditorTab(Song song, String locale) {
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
        Label localeLabel = new Label(locale);
        lyricsTab.setGraphic(localeLabel); // using a label to capture click event later
        lyricsTab.setOnSelectionChanged(event -> lastSelectedLocaleTab = index);
        lyricsTab.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(Labels.DIALOG_DELETE_LOCALE);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.CANCEL) {
                event.consume(); // stop propagation
            }
        });
        lyricsTab.setOnClosed(event -> lyricsEditorMap.remove(localeLabel.getText()));
        lyricsTab.setContent(lyricsEditor);
        titleLyricsTabs.getTabs().add(index, lyricsTab);

        localeLabel.setOnMouseClicked(event -> {
            // on double click, change locale
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                String newLocale = askForNewLocale();
                if (newLocale != null) {
                    String currentLocale = localeLabel.getText();
                    localeLabel.setText(newLocale);
                    lyricsEditorMap.remove(currentLocale);
                    lyricsEditorMap.put(newLocale, lyricsEditor);
                }
            }
        });
    }

    private String askForNewLocale() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(Labels.DIALOG_ADD_LOCALE);
        dialog.showAndWait();
        return dialog.getResult();
    }

    public void onCancel(ActionEvent event) {
        getStage().close();
    }

    public void onSave(ActionEvent event) {
        // TODO validations
        Song song = extractForm();

        songService.get().getStore().store(song);

        getStage().close();
    }

    private Song extractForm() {
        Song song = new Song(songId);
        song.setAuthors(StringUtils.split(authorsInput.getText()));
        song.setSongBook(songbookInput.getText());
        song.setEntry(songbookEntryInput.getText());
        song.setCopyright(copyrightInput.getText());
        song.setComments(commentsInput.getText());
        song.setPublisher(publisherInput.getText());
        song.setVerseOrder(verseOrderInput.getText());

        lyricsEditorMap.forEach((locale, lyricsEditor) -> {
            song.setTitle(locale, lyricsEditor.getTitle());
        });

        List<SongVerse> verses = new ArrayList<>();
        lyricsEditorMap.forEach((locale, lyricsEditor) -> {
            lyricsEditor.getVerses().forEach(verseNameAndText -> {
                verses.add(new SongVerse(verseNameAndText.getKey(), verseNameAndText.getValue(), locale));
            });
        });
        song.setVerses(verses);

        return song;
    }

    private void onAddVerseOrder(ActionEvent event) {
        String verseName = ((MenuItem)event.getSource()).getText();
        String verseOrder = verseOrderInput.getText();
        String newVerseOrder = String.join(", ", verseOrder, verseName);
        verseOrderInput.setText(newVerseOrder);
    }

    private Stage getStage() {
        return (Stage) verseOrderInput.getScene().getWindow();
    }

}
