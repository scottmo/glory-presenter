package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.Pair;
import com.scottmo.ui.components.Dialog;
import com.scottmo.ui.components.ListView;
import com.scottmo.ui.components.SongEditor;

public final class SongTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private SongService songService = ServiceProvider.get(SongService.class).get();

    // cache to look up song id
    private Map<String, Integer> songIdMap = new HashMap<>();

    private ListView songList = new ListView();
    private JButton buttonNewSong = new JButton(configService.getLabel("songs.buttonNewSong"));
    private JButton buttonEditSong = new JButton(configService.getLabel("songs.buttonEditSong"));
    private JButton buttonDeleteSong = new JButton(configService.getLabel("songs.buttonDeleteSong"));
    private JButton buttonDeselect = new JButton(configService.getLabel("songs.buttonDeselect"));
    private JButton buttonDuplicate = new JButton(configService.getLabel("songs.buttonDuplicate"));
    private JButton buttonGenerateGSlide = new JButton(configService.getLabel("songs.buttonGenerateGSlide"));
    private JButton buttonGeneratePPT = new JButton(configService.getLabel("songs.buttonGeneratePPT"));
    private JCheckBox checkboxStartSlide = new JCheckBox(configService.getLabel("songs.checkboxStartSlide"));
    private JCheckBox checkboxEndSlide = new JCheckBox(configService.getLabel("songs.checkboxEndSlide"));
    private JSpinner inputLinesPerSlide = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private SuggestionPicker inputTemplate = new SuggestionPicker(10);

    public SongTab() {
        loadSongList();

        updateButtonState();
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
                showSongEditor(songId);
            }
        });

        buttonDeleteSong.addActionListener(e -> {
            for (String songName : songList.getSelectedItems()) {
                Integer songId = songIdMap.get(songName);
                if (songId == null) {
                    Dialog.error(String.format("Unable to delete %s. Song cannot be found!", songName));
                } else {
                    songService.delete(songId);
                }
            }
        });

        buttonDeselect.addActionListener(e -> {
            songList.selectAll(false);
            updateButtonState();
        });

        inputTemplate.setText("./template/song.ppt");
        inputTemplate.setSuggestions(Arrays.asList(
            "./template/song.ppt",
            "./template/bible.ppt"
        ));
        inputTemplate.addActionListener(event -> System.out.println(inputTemplate.getText()));

        setLayout(new BorderLayout());
        add(row(UI_GAP,
            column(UI_GAP,
                cell(new JTextField()),
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
                cell(checkboxStartSlide),
                cell(checkboxEndSlide),
                cell(new JLabel(configService.getLabel("songs.inputLinesPerSlide"))),
                cell(inputLinesPerSlide),
                cell(new JLabel(configService.getLabel("songs.inputTemplate"))),
                cell(inputTemplate),
                cell(buttonGeneratePPT),
                cell(buttonGenerateGSlide)
            ).weightBy(1.0)
        ).with(view -> view.setBorder(new EmptyBorder(UI_GAP, UI_GAP, UI_GAP, UI_GAP)))
        .getComponent());
    }

    private void loadSongList() {
        List<Pair<Integer, String>> songs = songService.getAllSongDescriptors(configService.getConfig().getLocales());
        List<String> items = new ArrayList<>();
        songIdMap.clear();
        for (Pair<Integer,String> pair : songs) {
            Integer songId = pair.key();
            String songName = pair.value();
            songIdMap.put(songName, songId);
            items.add(songName);
        }
        Collections.sort(items);
        songList.setItems(items);
    }

    private void updateButtonState() {
        boolean oneSelected = songList.getSelectCount() == 1;
        boolean moreThanOneSelected = songList.getSelectCount() == 1;

        buttonEditSong.setEnabled(oneSelected);
        buttonDuplicate.setEnabled(oneSelected);
        buttonGenerateGSlide.setEnabled(oneSelected);
        buttonGeneratePPT.setEnabled(oneSelected);

        buttonDeleteSong.setEnabled(moreThanOneSelected);
        buttonDeselect.setEnabled(moreThanOneSelected);
    }

    private void showSongEditor(Integer id) {
        String title = id == null ? "songs.editor.titleNew" : "songs.editor.titleEdit";

        SongEditor songEditor = new SongEditor();
        JDialog modal = Dialog.newModal(configService.getLabel(title), songEditor);

        songEditor.addCancelListener(() -> {
            modal.dispose();
        });
        songEditor.addSaveListener((Song song) -> {
            songService.store(song);
        });

        modal.setVisible(true);
    }
}
