package com.scottmo.app.controllers;

import com.scottmo.app.Labels;
import com.scottmo.app.views.ViewUtil;
import com.scottmo.data.song.Song;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.scottmo.config.AppContext.PRIMARY_LOCALE;
import static com.scottmo.config.AppContext.SECONDARY_LOCALE;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);
    private final Map<String, Integer> songIdsMap = new HashMap<>();
    private ObservableList<String> items;

    public TextField searchInput;
    public ListView<String> songList;

    public void initialize() {
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

        items = FXCollections.observableArrayList();
        items.addAll(titles.values());
        songList.setItems(items);

        if (items.size() > 0) {
            songList.getSelectionModel().selectFirst();
        }
    }

    public void onSearchSong(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (searchInput.getText().isEmpty()) {
                // restore list when empty
                songList.setItems(items);
            } else {
                // search ignore case
                var filteredItems = items.filtered(item ->
                        item.toLowerCase().contains(searchInput.getText().toLowerCase()));
                songList.setItems(filteredItems);
            }
        }
    }

    public void onEditSong(ActionEvent event) throws IOException {
        if (songList.getItems().size() == 0) return;

        Stage verseEditorModal = ViewUtil.get().newModal(Labels.MODAL_EDIT_SONG_TITLE, VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        Song song = songService.get().getStore().get(getSelectedSongId());
        verseEditorModal.setUserData(song);
        verseEditorModal.show();
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal(Labels.MODAL_NEW_SONG_TITLE, VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new Song());
        verseEditorModal.showAndWait();
        refreshSongList();
    }

    public void onDeleteSong(ActionEvent event) {
        if (songList.getItems().size() == 0) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Labels.MODAL_DELETE_SONG_TITLE.formatted(getSelectedSong()),
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
