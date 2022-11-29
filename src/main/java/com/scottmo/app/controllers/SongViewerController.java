package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.data.song.Song;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.scottmo.config.Constants.PRIMARY_LOCALE;
import static com.scottmo.config.Constants.SECONDARY_LOCALE;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private Supplier<SongService> songService = ServiceSupplier.get(SongService.class);

    public ListView<String> songList;

    private Map<String, Integer> songIdsMap = new HashMap<>();

    public void initialize() {
        Platform.runLater(this::initSongList);
    }

    private void initSongList() {
        Map<Integer, String> titles = getSongTitles();
        // build invert lookup map
        for (var entry : titles.entrySet()) {
            if (songIdsMap.containsKey(entry.getValue())) continue;
            songIdsMap.put(entry.getValue(), entry.getKey());
        }

        ObservableList<String> items = FXCollections.observableArrayList();
        items.addAll(titles.values());
        songList.setItems(items);
    }

    public void onEditSong(ActionEvent event) throws IOException, ParserConfigurationException, SAXException {
        Stage verseEditorModal = ViewUtil.get().newModal("Edit Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        Integer songId = songIdsMap.get(songList.getSelectionModel().getSelectedItem());
        Song song = songService.get().getStore().get(songId);
        verseEditorModal.setUserData(song);
        verseEditorModal.show();
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("New Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new Song());
        verseEditorModal.show();
    }

    public void onDeleteSong(ActionEvent event) {
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
