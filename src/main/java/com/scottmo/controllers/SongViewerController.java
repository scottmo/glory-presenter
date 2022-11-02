package com.scottmo.controllers;

import com.scottmo.services.openLyrics.OpenLyrics;
import com.scottmo.views.ViewUtil;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/verseEditor.fxml";

    public void onEditSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("Edit Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.show();
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal("New Song", VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new OpenLyrics());
        verseEditorModal.show();
    }
}
