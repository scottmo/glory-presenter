package com.scottmo.app.controllers;

import com.scottmo.app.Labels;
import com.scottmo.app.views.ViewUtil;
import com.scottmo.config.AppContext;
import com.scottmo.data.song.Song;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.ppt.SongSlidesGenerator;
import com.scottmo.services.songs.SongService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.scottmo.config.AppContext.PRIMARY_LOCALE;
import static com.scottmo.config.AppContext.SECONDARY_LOCALE;

public class SongViewerController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);
    private final AppContext appContext = ServiceSupplier.getAppContext();

    private final Map<String, Integer> songIdsMap = new HashMap<>();
    private ObservableList<String> items;

    public TextField searchInput;
    public ListView<String> songList;
    public Label totalNumSong;

    public Spinner<Integer> linesPerSlideInput;
    public TextField templatePathInput;

    public void initialize() {
        Platform.runLater(() -> {
            refreshSongList();

            templatePathInput.setOnMouseClicked(this::selectTemplateFile);
            linesPerSlideInput.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        });
    }

    private void selectTemplateFile(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        File pptxTemplateDir = new File(appContext.getPPTXTemplate(""));
        fileChooser.setInitialDirectory(pptxTemplateDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PPTX files (*.pptx)", "*.pptx"));
        File selectedFile = fileChooser.showOpenDialog(getStage());
        // selected new template file
        if (selectedFile != null) {
            // show just the file name if file is in template folder, otherwise ues full path
            if (selectedFile.getParentFile().getAbsolutePath().equals(pptxTemplateDir.getAbsolutePath())) {
                templatePathInput.setText(selectedFile.getName());
            } else {
                templatePathInput.setText(selectedFile.getAbsolutePath());
            }
        }
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
        items.addAll(titles.values().stream().sorted().toList());
        songList.setItems(items);

        if (items.size() > 0) {
            songList.getSelectionModel().selectFirst();
        }
        totalNumSong.setText(String.valueOf(items.size()));
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
        Song song = loadSelectedSong();
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Labels.MODAL_DELETE_SONG_TITLE.formatted(getSelectedSongTitle()),
                ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            songService.get().getStore().delete(getSelectedSongId());
        }
        refreshSongList();
    }

    private String getSelectedSongTitle() {
        return songList.getSelectionModel().getSelectedItem();
    }

    private int getSelectedSongId() {
        return songIdsMap.get(getSelectedSongTitle());
    }

    private Song loadSelectedSong() {
        return songService.get().getStore().get(getSelectedSongId());
    }

    private Map<Integer, String> getSongTitles() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.get().getStore().getTitles(PRIMARY_LOCALE)) {
            titles.put(title.getKey(), title.getValue());
        }
        for (var title : songService.get().getStore().getTitles(SECONDARY_LOCALE)) {
            var songId = title.getKey();
            if (titles.containsKey(songId)) {
                titles.put(songId, titles.get(songId) + " - " + title.getValue());
            } else {
                titles.put(songId, title.getValue());
            }
        }
        return titles;
    }

    public void onGeneratePPTX(ActionEvent event) throws IOException {
        Song song = loadSelectedSong();
        String outputFilePath = appContext.getOutputDir(getSelectedSongTitle() + ".pptx");
        String templateFilePath = templatePathInput.getText();
        if (!templateFilePath.contains("/")) {
            templateFilePath = appContext.getPPTXTemplate(templateFilePath);
        }
        SongSlidesGenerator.generate(song, templateFilePath,
                outputFilePath, List.of("zh_cn", "en_us"), linesPerSlideInput.getValue());
    }

    public void onGenerateGSlides(ActionEvent event) {
    }

    private Stage getStage() {
        return (Stage) templatePathInput.getScene().getWindow();
    }
}
