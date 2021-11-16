package com.scottscmo.ui.container;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import com.scottscmo.config.Config;
import com.scottscmo.song.Song;
import com.scottscmo.song.adapters.SongSlideTextAdapter;
import com.scottscmo.song.adapters.SongYAMLAdapter;
import com.scottscmo.ui.components.C;

import net.miginfocom.swing.MigLayout;

public class SongFormatter extends JPanel {
    private static final String TRANSFORM_BUTTON = "Transform";
    private static final String MAX_LINES_INPUT_LABEL = "Lines Per Slide Per Language";
    private static final String SONG_SEARCH_INPUT_LABEL = "Search Song";

    private static final int SECTION_MARGIN = 10;

    private static final int SONG_LIST_WIDTH_PX = 400;
    private static final int SONG_LIST_HEIGHT_ROW = 10;
    private static final int SONG_LIST_FONT_SIZE = 16;

    private static final int SONG_VIEW_WIDTH_COL = 30;

    private static final int OUTPUT_VIEW_WIDTH_COL = 20;

    private static Vector<String> songTitles;

    public SongFormatter() {
        super();

        // components

        JList<String> songList = getSongList();;
        JTextArea songViewer = getSongViewer();
        JTextArea outputViewer = getOutputViewer();

        JLabel maxLinesLabel = new JLabel(MAX_LINES_INPUT_LABEL);
        JSpinner maxLinesInput = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        JButton transformButton = new JButton(TRANSFORM_BUTTON);

        JLabel songSearchInputLabel = new JLabel(SONG_SEARCH_INPUT_LABEL);
        JTextField songSearchInput = new JTextField();

        // behavior

        songList.addMouseListener(new MouseAdapter() { 
            @Override
            public void mouseReleased(MouseEvent me) {
                loadSong(songList, songViewer);
            } 
        });

        transformButton.addActionListener((ActionEvent e) -> {
            transformSong(songViewer, outputViewer, (Integer)maxLinesInput.getValue());
        });

        songSearchInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Vector<String> songTitles = getSongTitles().stream()
                        .filter(title -> title.contains(songSearchInput.getText()))
                        .collect(Collectors.toCollection(Vector::new));
                    if (songTitles.isEmpty()) {
                        songTitles = getSongTitles();
                    }
                    songList.setListData(songTitles);
                }
            }
        });

        // layout

        this.setLayout(new BorderLayout(SECTION_MARGIN, SECTION_MARGIN));

        JPanel songListPanel = new JPanel(new MigLayout("wrap 5"));
        songListPanel.add(songSearchInputLabel);
        songListPanel.add(songSearchInput, "span, align left");
        songSearchInput.setColumns(20);
        songListPanel.add(songList, "span");

        this.add(C.splitH(songListPanel, songViewer, outputViewer), BorderLayout.CENTER);

        JPanel transformPanel = new JPanel();
        transformPanel.add(maxLinesLabel);
        transformPanel.add(maxLinesInput);
        transformPanel.add(transformButton);
        this.add(transformPanel, BorderLayout.SOUTH);
    }

    // component init

    private JList<String> getSongList() {
        Vector<String> songTitles = getSongTitles();
        if (songTitles.isEmpty()) {
            songTitles = new Vector<String>();
            songTitles.add("Unable to load songs!");
        }
        JList<String> songList = new JList<>(songTitles);
        songList.setFixedCellHeight(SONG_LIST_FONT_SIZE);
        songList.setFixedCellWidth(SONG_LIST_WIDTH_PX);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setVisibleRowCount(SONG_LIST_HEIGHT_ROW);

        Config.subscribe(Config.DIR_DATA, dataPath -> {
            Vector<String> newSongTitles = getSongTitles(Path.of(dataPath, "songs"), true);
            if (newSongTitles.isEmpty()) {
                newSongTitles = new Vector<String>();
                newSongTitles.add("Unable to load songs!");
            }
            songList.setListData(newSongTitles);
        });

        return songList;
    }

    private JTextArea getSongViewer() {
        JTextArea textArea = new JTextArea();
        textArea.setColumns(SONG_VIEW_WIDTH_COL);
        return textArea;
    }

    private JTextArea getOutputViewer() {
        JTextArea textArea = new JTextArea();
        textArea.setColumns(OUTPUT_VIEW_WIDTH_COL);

        return textArea;
    }

    // functions

    private static Vector<String> getSongTitles() {
        return getSongTitles(Path.of(Config.get(Config.DIR_DATA), "songs"), false);
    }

    private static Vector<String> getSongTitles(Path dirSongPath, boolean bustCache) {
        if (!bustCache && songTitles != null) {
            return songTitles;
        }

        try {
            songTitles = Files.list(dirSongPath)
                .map(path -> path.getFileName().toString())
                .filter(path -> path.endsWith(".yaml"))
                .map(path -> path.replace(".yaml", ""))
                .sorted()
                .collect(Collectors.toCollection(Vector::new));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            songTitles = new Vector<String>();
        }

        return songTitles;
    }

    private static void loadSong(JList songList, JTextArea songViewer) {
        String songName = (String)songList.getSelectedValue();
        String songContent = SongYAMLAdapter.getSongFileContent(songName);
        if (songContent == null) {
            songContent = "Error getting content for song " + songName;
        }
        songViewer.setText(songContent);
    }

    private static void transformSong(JTextArea inputViewer, JTextArea outputViewer, Integer linesPerSlidePerLang) {
        Song song = SongYAMLAdapter.deserialize(inputViewer.getText());
        String output = SongSlideTextAdapter.serialize(song, Arrays.asList("zh", "en"),
                linesPerSlidePerLang.intValue());
        outputViewer.setText(output);
    }
}
