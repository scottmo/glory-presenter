package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.data.song.Song;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.scottmo.config.Constants.PRIMARY_LOCALE;
import static com.scottmo.config.Constants.SECONDARY_LOCALE;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);
    private final Map<String, Integer> songIdsMap = new HashMap<>();

    @FXML
    private ListView<String> songList;

    @FXML
    private void initialize() {
        Platform.runLater(this::refreshSongList);
    }

    private void refreshSongList() {
        Map<Integer, String> titles = getSongTitles();
        // build invert lookup map
        songIdsMap.clear();
        for (var entry : titles.entrySet()) {
            if (songIdsMap.containsKey(entry.getValue())) continue;
            songIdsMap.put(entry.getValue(), entry.getKey());
        }

        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll(titles.values());
        songList.setItems(items);

        if (items.size() > 0) {
            songList.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onEditSong(ActionEvent event) throws IOException {
        if (songList.getItems().size() == 0) return;

        Stage verseEditorModal = ViewUtil.get().newModal("Edit Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        Song song = songService.get().getStore().get(getSelectedSongId());
        verseEditorModal.setUserData(song);
        verseEditorModal.show();
    }

    @FXML
    private void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("New Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new Song());
        verseEditorModal.showAndWait();
        refreshSongList();
    }

    @FXML
    private void onDeleteSong(ActionEvent event) {
        if (songList.getItems().size() == 0) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete [" + getSelectedSong() + "] ?",
                ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            songService.get().getStore().delete(getSelectedSongId());
        }
        refreshSongList();
    }

    private String getSelectedSong() {
        return songList.getSelectionModel().getSelectedItem();
    }

    private int getSelectedSongId() {
        return songIdsMap.get(getSelectedSong());
    }

    private Map<Integer, String> getSongTitles() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.get().getStore().getTitles(PRIMARY_LOCALE)) {
            titles.put(title.getKey(), title.getValue());
        }
        for (var title : songService.get().getStore().getTitles(SECONDARY_LOCALE)) {
            var songId = title.getKey();
            if (titles.containsKey(songId)) {
                titles.put(songId, titles.get(songId) + " | " + title.getValue());
            } else {
                titles.put(songId, title.getValue());
            }
        }
        return titles;
    }
}
