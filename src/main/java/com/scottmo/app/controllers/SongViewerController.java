package com.scottmo.app.controllers;

import com.scottmo.data.openLyrics.OpenLyrics;
import com.scottmo.app.views.ViewUtil;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    public void onEditSong(ActionEvent event) throws IOException, ParserConfigurationException, SAXException {
        Stage verseEditorModal = ViewUtil.get().newModal("Edit Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        String song = Files.readString(Path.of("./10,000 Reasons.xml"));
        verseEditorModal.setUserData(OpenLyrics.of(song));
        verseEditorModal.show();
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("New Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new OpenLyrics());
        verseEditorModal.show();
    }
}
