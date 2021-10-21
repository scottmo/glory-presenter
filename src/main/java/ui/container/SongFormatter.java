package ui.container;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import song.SlideTextTransformer;
import song.Song;
import song.SongObjectMapper;

public class SongFormatter extends JPanel {
    private static final String TRANSFORM_BUTTON = "Transform";
    private static final String MAX_LINES_INPUT_LABEL = "Lines Per Slide Per Language";

    private static final int SECTION_MARGIN = 10;

    private static final int SONG_LIST_WIDTH_PX = 400;
    private static final int SONG_LIST_HEIGHT_ROW = 10;
    private static final int SONG_LIST_FONT_SIZE = 16;

    private static final int SONG_VIEW_WIDTH_COL = 30;

    private static final int OUTPUT_VIEW_WIDTH_COL = 20;

    public SongFormatter() {
        super();

        // components

        JList songList = getSongList();;
        JTextArea songViewer = getSongViewer();
        JTextArea outputViewer = getOutputViewer();
        JLabel maxLinesLabel = new JLabel(MAX_LINES_INPUT_LABEL);
        JSpinner maxLinesInput = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
        JButton transformButton = new JButton(TRANSFORM_BUTTON);

        // behavior

        songList.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) {
                loadSong(songList, songViewer);
            } 
        });

        transformButton.addActionListener((ActionEvent e) -> {
            transformSong(songViewer, outputViewer, (Integer)maxLinesInput.getValue());
        });

        // layout

        this.setLayout(new BorderLayout(SECTION_MARGIN, SECTION_MARGIN));

        JSplitPane sp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(songList), new JScrollPane(songViewer));
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                sp1, new JScrollPane(outputViewer));  
        this.add(sp, BorderLayout.CENTER);

        JPanel transformPanel = new JPanel();
        transformPanel.add(maxLinesLabel);
        transformPanel.add(maxLinesInput);
        transformPanel.add(transformButton);
        this.add(transformPanel, BorderLayout.SOUTH);
    }

    // component init

    private JList getSongList() {
        List<String> songTitles = getSongTitles();
        if (songTitles.isEmpty()) {
            songTitles = List.of("Unable to load songs!");
        }
        JList songList = new JList<>(getSongTitles().toArray());
        songList.setFixedCellHeight(SONG_LIST_FONT_SIZE);
        songList.setFixedCellWidth(SONG_LIST_WIDTH_PX);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setVisibleRowCount(SONG_LIST_HEIGHT_ROW);

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

    private static List<String> getSongTitles() {
        List<String> songTitles;

        try {
            songTitles = Files.list(Path.of("resources/songs"))
                .map(path -> path.getFileName().toString())
                .filter(path -> path.endsWith(".yaml"))
                .map(path -> path.replace(".yaml", ""))
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            songTitles = Collections.emptyList();
        }

        return songTitles;
    }

    private static void loadSong(JList songList, JTextArea songViewer) {
        String songName = (String)songList.getSelectedValue();
        String songContent = SongObjectMapper.getSongFileContent(songName);
        if (songContent == null) {
            songContent = "Error getting content for song " + songName;
        }
        songViewer.setText(songContent);
    }

    private static void transformSong(JTextArea inputViewer, JTextArea outputViewer, Integer linesPerSlidePerLang) {
        Song song = SongObjectMapper.deserialize(inputViewer.getText());
        String output = SlideTextTransformer.transform(song, Arrays.asList("zh", "en"),
                linesPerSlidePerLang.intValue()).stream()
            .collect(Collectors.joining("\n\n---\n\n"));
        outputViewer.setText(output);
    }
}
