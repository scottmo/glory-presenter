package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.scottmo.config.Constants.PRIMARY_LOCALE;
import static com.scottmo.config.Constants.SECONDARY_LOCALE;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private Supplier<SongService> songService = ServiceSupplier.get(SongService.class);

    public VBox songList;

    public void initialize() {
        Platform.runLater(this::initSongList);
    }

    private void initSongList() {
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
        titles.forEach((key, value) -> {
            Label titleLabel = new Label(value);
            titleLabel.getProperties().put("id", key);
            songList.getChildren().add(titleLabel);
        });
    }

    public void onEditSong(ActionEvent event) throws IOException, ParserConfigurationException, SAXException {
        Stage verseEditorModal = ViewUtil.get().newModal("Edit Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        String song = Files.readString(Path.of("./10,000 Reasons.xml"));
        //verseEditorModal.setUserData(OpenLyrics.of(song));
        verseEditorModal.show();
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("New Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        //verseEditorModal.setUserData(new OpenLyrics());
        verseEditorModal.show();
    }
}
