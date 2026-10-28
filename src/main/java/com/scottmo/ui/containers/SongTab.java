package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.operations.SongOperations;
import com.scottmo.shared.StringUtils;
import com.scottmo.ui.components.ListView;
import com.scottmo.ui.components.SongEditor;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.FilePicker;

public final class SongTab extends JPanel {
    private final ConfigService configService = ConfigService.get();
    private final SongOperations songOperations = SongOperations.forApplication();

    // cache to look up song id
    private Map<String, Integer> songIdMap = new HashMap<>();
    private List<String> songNames;

    private final ListView songList = new ListView();

    private final JButton buttonNewSong = new JButton(Labels.get("songs.buttonNewSong"));
    private final JButton buttonEditSong = new JButton(Labels.get("songs.buttonEditSong"));
    private final JButton buttonDeleteSong = new JButton(Labels.get("songs.buttonDeleteSong"));
    private final JButton buttonDeselect = new JButton(Labels.get("songs.buttonDeselect"));
    private final JButton buttonDuplicate = new JButton(Labels.get("songs.buttonDuplicate"));

    private final JButton buttonImport = new JButton(Labels.get("songs.buttonImport"));
    private final JButton buttonExport = new JButton(Labels.get("songs.buttonExport"));
    private final JButton buttonExportYaml = new JButton(Labels.get("songs.buttonExportYaml"));

    private final JButton buttonGeneratePPT = new JButton(Labels.get("songs.buttonGeneratePPT"));
    private final JSpinner inputLinesPerSlide = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final SuggestionPicker inputTemplate = new SuggestionPicker(10);

    private final JComponent mainView;

    public SongTab() {
        loadSongList();
        updateButtonState();

        var inputSearch = new JTextField();
        inputSearch.addActionListener(e -> { // on ENTER key
            String searchText = inputSearch.getText().trim();
            List<String> filteredSongNames = searchText.isEmpty()
                ? songNames
                : songNames.stream() .filter(s -> s.toLowerCase().contains(searchText.toLowerCase())) .collect(Collectors.toList());
            songList.setItems(filteredSongNames);
        });

        songList.setSelectionListener((String item, boolean selected) -> {
            updateButtonState();
        });

        buttonNewSong.addActionListener(e -> {
            showSongEditor(null);
        });

        buttonEditSong.addActionListener(e -> {
            String songName = songList.getSelectedItems().get(0);
            Integer songId = songIdMap.get(songName);
            if (songId == null) {
                Dialog.error(String.format("Unable to edit %s. Song cannot be found!", songName));
            } else {
                showSongEditor(songOperations.get(songId));
            }
        });

        buttonDuplicate.addActionListener(e -> {
            String songName = songList.getSelectedItems().get(0);
            Integer songId = songIdMap.get(songName);
            if (songId == null) {
                Dialog.error(String.format("Unable to duplicate %s. Song cannot be found!", songName));
            } else {
                Song song = songOperations.duplicateDraft(songId);
                showSongEditor(song);
            }
        });

        buttonDeleteSong.addActionListener(e -> {
            List<String> deletedSongs = new ArrayList<>();
            List<Integer> deletedSongIds = new ArrayList<>();
            for (String songName : songList.getSelectedItems()) {
                Integer songId = songIdMap.get(songName);
                if (songId == null) {
                    Dialog.error(String.format("Unable to delete %s. Song cannot be found!", songName));
                } else {
                    deletedSongIds.add(songId);
                    deletedSongs.add(songName);
                }
            }
            if (!deletedSongIds.isEmpty()) {
                songOperations.delete(deletedSongIds);
                Dialog.info(String.format("Deleted songs: %s", StringUtils.join(deletedSongs)));
                loadSongList();
            }
        });

        buttonImport.addActionListener(evt -> {
            FilePicker.show(selectedFilePath -> {
                try {
                    importSongs(selectedFilePath);
                    Dialog.info("Import success!");
                    loadSongList();
                } catch (Exception e) {
                    Dialog.error("Error importing " + selectedFilePath, e);
                }
            });
        });

        buttonExport.addActionListener(evt -> {
            List<String> exportedSongs = new ArrayList<>();
            for (String songName : songList.getSelectedItems()) {
                Integer songId = songIdMap.get(songName);
                if (songId == null) {
                    Dialog.error(String.format("Unable to export %s. Song cannot be found!", songName));
                } else {
                    try {
                        exportSong(songId);
                        exportedSongs.add(songName);
                    } catch (IOException ioe) {
                        Dialog.error(String.format("Failed to export %s!", songName), ioe);
                    }
                }
            }
            if (!exportedSongs.isEmpty()) {
                Dialog.info(String.format("Exported songs: %s", StringUtils.join(exportedSongs)));
            }
        });

        buttonExportYaml.addActionListener(evt -> {
            List<String> exportedSongs = new ArrayList<>();
            for (String songName : songList.getSelectedItems()) {
                Integer songId = songIdMap.get(songName);
                if (songId == null) {
                    Dialog.error(String.format("Unable to export %s. Song cannot be found!", songName));
                } else {
                    try {
                        exportSongToYaml(songId);
                        exportedSongs.add(songName);
                    } catch (IOException ioe) {
                        Dialog.error(String.format("Failed to export %s to YAML!", songName), ioe);
                    }
                }
            }
            if (!exportedSongs.isEmpty()) {
                Dialog.info(String.format("Exported songs to YAML: %s", StringUtils.join(exportedSongs)));
            }
        });

        buttonDeselect.addActionListener(e -> {
            songList.selectAll(false);
            updateButtonState();
        });

        buttonGeneratePPT.addActionListener(evt -> {
            if (songList.getSelectCount() != 1) {
                return;
            }
            String songName = songList.getSelectedItems().get(0);
            Integer songId = songIdMap.get(songName);
            try {
                String outputPath = songOperations.generatePowerpoint(songId, getLinesPerSlide(),
                    inputTemplate.getText(), null);
                Dialog.info("Successfully generated ppt at " + outputPath);
            } catch (IOException e) {
                Dialog.error("Unable to generate ppt", e);
            }
        });

        List<String> templatePaths = new ArrayList<>(configService.getConfig().getTemplatePaths());
        inputTemplate.setText(templatePaths.stream().filter(path -> path.toLowerCase().contains("song")).findFirst().orElse(""));
        inputTemplate.setSuggestions(templatePaths);

        setLayout(new BorderLayout());
        mainView = row(UI_GAP,
            column(UI_GAP,
                cell(inputSearch),
                cell(new JLabel("Note: click on empty space of the row to select single row")),
                cell(songList).weightBy(1.0) // take as much space as possible
            ).weightBy(4.0),
            column(UI_GAP,
                strut(25),
                cell(buttonNewSong),
                cell(buttonEditSong),
                cell(buttonDuplicate),
                cell(buttonDeleteSong),
                cell(buttonDeselect),
                cell(new JSeparator()),
                cell(buttonImport),
                cell(buttonExport),
                cell(buttonExportYaml),
                cell(new JSeparator()),
                cell(new JLabel(Labels.get("songs.inputLinesPerSlide"))),
                cell(inputLinesPerSlide),
                cell(new JLabel(Labels.get("songs.inputTemplate"))),
                cell(inputTemplate),
                cell(buttonGeneratePPT)
            ).weightBy(1.0)
        ).getComponent();
        add(mainView, BorderLayout.CENTER);
    }

    private int getLinesPerSlide() {
        return (Integer) inputLinesPerSlide.getValue();
    }

    private void loadSongList() {
        songIdMap = getSongIdMap();
        songNames = new ArrayList<>(songIdMap.keySet());
        songNames.sort(String.CASE_INSENSITIVE_ORDER);
        songList.setItems(songNames);
        updateButtonState();
    }

    public void refreshSongList() {
        loadSongList();
    }

    private void updateButtonState() {
        boolean onlyOneSelected = songList.getSelectCount() == 1;
        boolean hasSelection = songList.getSelectCount() > 0;

        buttonEditSong.setEnabled(onlyOneSelected);
        buttonDuplicate.setEnabled(onlyOneSelected);
        buttonGeneratePPT.setEnabled(onlyOneSelected);

        buttonDeleteSong.setEnabled(hasSelection);
        buttonDeselect.setEnabled(hasSelection);
        buttonExport.setEnabled(hasSelection);
        buttonExportYaml.setEnabled(onlyOneSelected);
    }

    private void showSongEditor(Song song) {
        SongEditor songEditor = new SongEditor(song);

        songEditor.addCancelListener(this::restoreMainView);
        songEditor.addSaveListener((Song modifiedSong) -> {
            songOperations.save(modifiedSong);
            Dialog.info(String.format("Saved song '%s' successfully!", modifiedSong.getTitle()));
            restoreMainView();
            loadSongList(); // refresh song list
        });

        switchView(songEditor);
    }

    private void switchView(JComponent component) {
        removeAll();
        add(component, BorderLayout.CENTER);
        revalidate();
        repaint();
        component.requestFocusInWindow();
    }

    private void restoreMainView() {
        switchView(mainView);
    }

    private void importSongs(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        try {
            songOperations.importOpenLyrics(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to import song [%s]!".formatted(filePath), e);
        }
    }

    private void exportSong(Integer id) throws IOException {
        songOperations.exportOpenLyrics(id, Path.of(songOperations.defaultOpenLyricsPath(id)));
    }

    private void exportSongToYaml(Integer id) throws IOException {
        songOperations.exportYaml(id, Path.of(songOperations.defaultYamlPath(id)));
    }

    private Map<String, Integer> getSongIdMap() {
        Map<String, Integer> titles = new HashMap<>();
        List<SongOperations.SongSummary> songs = songOperations.list(null, null);
        Map<String, Long> counts = songs.stream().collect(Collectors.groupingBy(
            SongOperations.SongSummary::descriptor, Collectors.counting()));
        for (SongOperations.SongSummary song : songs) {
            String label = song.descriptor();
            if (counts.getOrDefault(label, 0L) > 1) {
                label = label + " [#" + song.id() + "]";
            }
            titles.put(label, song.id());
        }
        return titles;
    }
}
