package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.StringUtils;
import com.scottmo.ui.components.ListView;
import com.scottmo.ui.components.SongEditor;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.FilePicker;

public final class SongTab extends JPanel {
    private final ConfigService configService = ConfigService.get();
    private final SongService songService = ServiceProvider.get(SongService.class).get();
    private final PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

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

    private final JButton buttonGenerateGSlide = new JButton(Labels.get("songs.buttonGenerateGSlide"));
    private final JButton buttonGeneratePPT = new JButton(Labels.get("songs.buttonGeneratePPT"));
    private final JSpinner inputLinesPerSlide = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final SuggestionPicker inputTemplate = new SuggestionPicker(10);

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
                showSongEditor(songService.get(songId));
            }
        });

        buttonDuplicate.addActionListener(e -> {
            String songName = songList.getSelectedItems().get(0);
            Integer songId = songIdMap.get(songName);
            if (songId == null) {
                Dialog.error(String.format("Unable to duplicate %s. Song cannot be found!", songName));
            } else {
                Song song = songService.get(songId);
                song.resetId();
                showSongEditor(song);
            }
        });

        buttonDeleteSong.addActionListener(e -> {
            List<String> deletedSongs = new ArrayList<>();
            for (String songName : songList.getSelectedItems()) {
                Integer songId = songIdMap.get(songName);
                if (songId == null) {
                    Dialog.error(String.format("Unable to delete %s. Song cannot be found!", songName));
                } else {
                    songService.delete(songId);
                    deletedSongs.add(songName);
                }
            }
            if (!deletedSongs.isEmpty()) {
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

        buttonDeselect.addActionListener(e -> {
            songList.selectAll(false);
            updateButtonState();
        });

        buttonGeneratePPT.addActionListener(evt -> {
            String songName = songList.getSelectedItems().get(0);
            Integer songId = songIdMap.get(songName);
            try {
                String outputPath = generatePowerpoint(songId, songName, getLinesPerSlide(), inputTemplate.getText());
                Dialog.info("Successfully generated ppt at " + outputPath);
            } catch (IOException e) {
                Dialog.error("Unable to generate ppt", e);
            }
        });

        List<String> templatePaths = new ArrayList<>(configService.getConfig().getTemplatePaths());
        inputTemplate.setText(templatePaths.stream().filter(path -> path.toLowerCase().contains("song")).findFirst().orElse(""));
        inputTemplate.setSuggestions(templatePaths);

        setLayout(new BorderLayout());
        add(row(UI_GAP,
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
                cell(new JSeparator()),
                cell(new JLabel(Labels.get("songs.inputLinesPerSlide"))),
                cell(inputLinesPerSlide),
                cell(new JLabel(Labels.get("songs.inputTemplate"))),
                cell(inputTemplate),
                cell(buttonGeneratePPT),
                cell(buttonGenerateGSlide)
            ).weightBy(1.0)
        ).getComponent());
    }

    private int getLinesPerSlide() {
        return (Integer) inputLinesPerSlide.getValue();
    }

    private void loadSongList() {
        songIdMap = getSongIdMap();
        songNames = new ArrayList<>(songIdMap.keySet());
        Collections.sort(songNames);
        songList.setItems(songNames);
    }

    private void updateButtonState() {
        boolean onlyOneSelected = songList.getSelectCount() == 1;
        boolean hasSelection = songList.getSelectCount() > 0;

        buttonEditSong.setEnabled(onlyOneSelected);
        buttonDuplicate.setEnabled(onlyOneSelected);
        buttonGenerateGSlide.setEnabled(onlyOneSelected);
        buttonGeneratePPT.setEnabled(onlyOneSelected);

        buttonDeleteSong.setEnabled(hasSelection);
        buttonDeselect.setEnabled(hasSelection);
        buttonExport.setEnabled(hasSelection);
    }

    private void showSongEditor(Song song) {
        String title = song == null || song.getId() == -1
            ? "songs.editor.titleNew"
            : "songs.editor.titleEdit";

        SongEditor songEditor = new SongEditor(song);
        JDialog modal = Dialog.newModal(Labels.get(title), songEditor);

        songEditor.addCancelListener(modal::dispose);
        songEditor.addSaveListener((Song modifiedSong) -> {
            songService.store(modifiedSong);
            Dialog.info(String.format("Saved song '%s' successfully!", modifiedSong.getTitle()));
            modal.dispose();
            loadSongList(); // refresh song list
        });

        modal.setVisible(true);
    }

    private void importSongs(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // TODO handle importing a zip of songs
        try {
            String content = Files.readString(Path.of(filePath));
            songService.importSong(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import song [%s]!".formatted(filePath), e);
        }
    }

    private void exportSong(Integer id) throws IOException {
        Song song = songService.get(id);
        String outputPath = configService.getOutputPath(StringUtils.sanitizeFilename(song.getTitle()) + ".xml");
        String songXML = songService.serialize(song);
        Files.writeString(Path.of(outputPath), songXML, StandardCharsets.UTF_8);
    }

    private Map<String, Integer> getSongIdMap() {
        Map<String, Integer> titles = new HashMap<>();
        for (var title : songService.getAllSongDescriptors(configService.getConfig().getLocales())) {
            titles.put(title.value(), title.key());
        }
        return titles;
    }

    private String generatePowerpoint(Integer id, String songName, Integer linesPerSlide, String templatePath)
            throws IOException {
        Song song = songService.get(id);
        String outputPath = configService.getOutputPath(StringUtils.sanitizeFilename(songName) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = configService.getPowerpointTemplate(templatePath);
        }
        powerpointService.generate(song, templatePath, outputPath, linesPerSlide);
    
        return outputPath;
    }
}
