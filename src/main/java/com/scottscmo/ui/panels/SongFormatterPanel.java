package com.scottscmo.ui.panels;

import com.scottscmo.AppLogger;
import com.scottscmo.Config;
import com.scottscmo.model.content.ContentUtil;
import com.scottscmo.ui.components.FileEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SongFormatterPanel extends JPanel {

    public SongFormatterPanel() {
        var defaultSongDir = Path.of(Config.getRelativePath(Config.CONTENTS_DIR));
        var songEditor = new FileEditor(defaultSongDir, "Select Song");
        var maxLinesSpinnerInput = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        var transformButton = new JButton("Transform");
        var saveTransformedButton = new JButton("Save as Slide-Format Song");
        var outputTextArea = new JTextArea(25, 45);

        outputTextArea.setFont(Config.getTextAreaFont());

        var defaultSongSlidesDir = Path.of(Config.getRelativePath(Config.CONTENTS_SLIDE_DIR));
        var songSlideEditor = new FileEditor(defaultSongSlidesDir, "Select Stored Slide-Format Song");

        setLayout(new MigLayout("wrap 3", "sg main, grow, left", "top"));

        // song picker
        add(songEditor.getUI());
        // song to slide text/csv transformer
        var songTransformerContainer = new JPanel();
        songTransformerContainer.setLayout(new MigLayout("wrap, ins 0"));
        var songTransformerHeader = new JPanel();
        songTransformerHeader.setLayout(new MigLayout("ins 0"));
        songTransformerHeader.add(transformButton);
        songTransformerHeader.add(maxLinesSpinnerInput);
        songTransformerHeader.add(new JLabel("Lines/Section/Text Group"));
        songTransformerContainer.add(songTransformerHeader, "span");
        songTransformerContainer.add(new JScrollPane(outputTextArea), "span, grow");
        songTransformerContainer.add(saveTransformedButton);
        add(songTransformerContainer);
        // slide text
        add(songSlideEditor.getUI());

        // controls
        transformButton.addActionListener(e -> {
            handleTransformSong(songEditor.getContent(), (Integer)maxLinesSpinnerInput.getValue(), outputTextArea);
        });

        saveTransformedButton.addActionListener(e -> {
            var filePath = Config.getRelativePath("%s/%s".formatted(Config.CONTENTS_SLIDE_DIR, Path.of(songEditor.getPath()).getFileName()));
            handleSaveTransformed(filePath, outputTextArea.getText());
        });
    }

    private static void handleTransformSong(String serializedSong, int maxLines, JTextArea outputTextArea) {
        try {
            var song = ContentUtil.parse(serializedSong);
            if (song != null) {
                var transformedText = ContentUtil.stringify(song, Config.get().googleSlideConfig().textConfigsOrder(), maxLines);
                outputTextArea.setText(transformedText);
                outputTextArea.setCaretPosition(0); // scroll to top
            }
        } catch (IOException e) {
            AppLogger.showError("Unable to transform song!", e);
        }
    }

    private static void handleSaveTransformed(String filePath, String serializedSong) {
        try {
            // try to parse it first before saving as a way to validate data
            ContentUtil.parse(serializedSong);
        } catch (IOException e) {
            AppLogger.showError("Invalid song format!", e);
        }

        try {
            Files.writeString(Path.of(filePath), serializedSong);
        } catch (IOException e) {
            AppLogger.showError("Unable to save transformed song!", e);
        }
    }
}
