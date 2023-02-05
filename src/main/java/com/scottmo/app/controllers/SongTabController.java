package com.scottmo.app.controllers;

import com.scottmo.app.Labels;
import com.scottmo.app.views.ViewUtil;
import com.scottmo.config.AppContext;
import com.scottmo.data.song.Song;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.logging.AppLoggerService;
import com.scottmo.services.ppt.SongSlidesGenerator;
import com.scottmo.services.songs.SongService;
import com.scottmo.util.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SongTabController {
    private static final String VERSE_EDITOR_FXML = "/ui/songEditor.fxml";

    private final AppContext appContext = ServiceSupplier.getAppContext();
    private final Supplier<SongService> songService = ServiceSupplier.get(SongService.class);
    private final Supplier<SongSlidesGenerator> pptxGenerator = ServiceSupplier.get(SongSlidesGenerator.class);
    private final Supplier<AppLoggerService> logger = ServiceSupplier.get(AppLoggerService.class);

    private final Map<String, Integer> songIdsMap = new HashMap<>();
    public CheckBox hasStartSlide;
    public CheckBox hasEndSlide;
    private ObservableList<String> items;

    public TextField searchInput;
    public ListView<String> songList;
    public Button editButton;
    public Button deleteButton;
    public Button exportButton;
    public Label totalNumSong;
    public Spinner<Integer> linesPerSlideInput;
    public TextField templatePathInput;

    public void initialize() {
        Platform.runLater(() -> {
            refreshSongList();

            ViewUtil.get().attachFilePickerToInput(templatePathInput, appContext.getPPTXTemplate(""), ViewUtil.FILE_EXT_PPTX);
            linesPerSlideInput.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        });
    }

    public void onSearchSong(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (searchInput.getText().isEmpty()) {
                // restore list when empty
                songList.setItems(items);
                toggleSongActionButtons(items.size() > 0);
            } else {
                // search ignore case
                var filteredItems = items.filtered(item ->
                        item.toLowerCase().contains(searchInput.getText().toLowerCase()));
                songList.setItems(filteredItems);

                toggleSongActionButtons(filteredItems.size() > 0);
            }
        }
    }

    public void onNewSong(ActionEvent event) throws IOException {
        Stage verseEditorModal = ViewUtil.get().newModal(Labels.MODAL_NEW_SONG_TITLE, VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        verseEditorModal.setUserData(new Song());
        verseEditorModal.showAndWait();
        refreshSongList();
    }

    public void onEditSong(ActionEvent event) throws IOException {
        if (songList.getItems().size() == 0) return;

        Stage verseEditorModal = ViewUtil.get().newModal(Labels.MODAL_EDIT_SONG_TITLE, VERSE_EDITOR_FXML, ViewUtil.get().getOwnerWindow(event));
        Song song = loadSelectedSong();
        verseEditorModal.setUserData(song);
        verseEditorModal.show();
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

    public void onGeneratePPTX(ActionEvent event) {
        Song song = loadSelectedSong();
        String outputFilePath = appContext.getOutputDir(StringUtils.sanitizeFilename(getSelectedSongTitle()) + ".pptx");
        String templateFilePath = templatePathInput.getText();
        if (!templateFilePath.contains("/")) {
            templateFilePath = appContext.getPPTXTemplate(templateFilePath);
        }
        try {
            pptxGenerator.get().generate(song, templateFilePath, outputFilePath, appContext.getConfig().locales(),
                    linesPerSlideInput.getValue(), hasStartSlide.isSelected(), hasEndSlide.isSelected());
            logger.get().info("Generated slides at " + outputFilePath);
        } catch (IOException e) {
            logger.get().error("Failed to generate slides!", e);
        }
    }

    public void onImportSong(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(ViewUtil.FILE_EXT_OPENLYRICS);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getStage());
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                try {
                    String openLyricsXML = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    Song song = songService.get().getOpenLyricsConverter().deserialize(openLyricsXML);
                    songService.get().getStore().store(song);
                } catch (IOException e) {
                    logger.get().error("Failed to import song [%s]!".formatted(file.getName()), e);
                }
            }
            refreshSongList();
            logger.get().info("Done importing songs!");
        }
    }

    public void onExportSong(ActionEvent event) {
        Song song = loadSelectedSong();
        String songTitle = getSelectedSongTitle();
        String outputFilePath = appContext.getOutputDir(songTitle + ".xml");
        String songXML = songService.get().getOpenLyricsConverter().serialize(song);
        try {
            Files.writeString(Path.of(outputFilePath), songXML, StandardCharsets.UTF_8);
            logger.get().info("Exported song at " + outputFilePath);
        } catch (IOException e) {
            logger.get().error("Failed to export song [%s]!".formatted(songTitle), e);
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

        boolean hasItems = items.size() > 0;
        if (hasItems) {
            songList.getSelectionModel().selectFirst();
        }
        toggleSongActionButtons(hasItems);
        totalNumSong.setText(String.valueOf(items.size()));
    }

    private void toggleSongActionButtons(boolean isEnabled) {
        if (isEnabled) {
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            exportButton.setDisable(false);
        } else {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            exportButton.setDisable(true);
        }
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
        for (var title : songService.get().getStore().getAllSongDescriptors(appContext.getConfig().locales())) {
            titles.put(title.getKey(), title.getValue());
        }
        return titles;
    }

    private Stage getStage() {
        return ViewUtil.get().getStage(templatePathInput);
    }
}
