package com.scottscmo.ui.components;

import com.scottscmo.Config;
import com.scottscmo.ui.FilePicker;
import com.scottscmo.ui.OutputDisplay;
import net.miginfocom.swing.MigLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;

public final class FileEditor {
    private static final int DEFAULT_HEIGHT = 25;
    private static final int DEFAULT_WIDTH = 45;

    private String filePath = "";

    private final JPanel ui;
    private final JButton filePicker;
    private final JTextArea textArea;
    private final JButton saveButton;
    private final JButton reloadButton;

    public FileEditor(Path path, String filePickerLabel, int editorHeight, int editorWidth) throws IOException {
        // ui
        ui = new JPanel();
        saveButton = new JButton("Save");
        reloadButton = new JButton("Reload");
        filePicker = new JButton(filePickerLabel);
        textArea = new JTextArea(editorHeight, editorWidth);

        ui.setLayout(new MigLayout("ins 0"));
        ui.add(filePicker, "wrap, span, growx");
        ui.add(new JScrollPane(textArea), "wrap, span, grow");
        ui.add(reloadButton);
        ui.add(saveButton);

        // listeners
        boolean isFilePickerEnabled = Files.isDirectory(path);
        if (isFilePickerEnabled) {
            filePicker.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent me) {
                    FilePicker.show(FilePicker.SEARCHABLE_DIRECTORY, path.toString(), selectedPath -> {
                        filePath = selectedPath;
                        loadFileToTextArea(filePath, textArea);
                        toggleReadWriteButtons(true);
                    });
                }
            });
        } else {
            filePath = path.toString();
            filePicker.setText("Load " + filePath);
            filePicker.addActionListener(e -> {
                filePicker.setEnabled(false);
                filePicker.setText(filePath);
                loadFileToTextArea(filePath, textArea);
                toggleReadWriteButtons(true);
            });
        }

        saveButton.addActionListener(e -> {
            try {
                Files.writeString(Path.of(filePath), textArea.getText());
            } catch (IOException ioe) {
                OutputDisplay.error("Unable to load " + filePath, ioe);
            }
        });

        reloadButton.addActionListener(e -> {
            loadFileToTextArea(filePath, textArea);
        });

        toggleReadWriteButtons(false);
    }

    public FileEditor(Path path, String filePickerLabel) throws IOException {
        this(path, filePickerLabel, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    public FileEditor(String path, String filePickerLabel, int editorHeight, int editorWidth) throws IOException {
        this(Path.of(Config.getRelativePath(path)), filePickerLabel, editorHeight, editorWidth);
    }

    public FileEditor(String path, String filePickerLabel) throws IOException {
        this(path, filePickerLabel, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    public JComponent getUI() {
        return ui;
    }

    public String getContent() {
        return textArea.getText();
    }

    public String getPath() {
        return filePath;
    }

    public void toggleReadWriteButtons(boolean isEnabled) {
        saveButton.setEnabled(isEnabled);
        reloadButton.setEnabled(isEnabled);
    }

    private static void loadFileToTextArea(String path, JTextArea textArea) {
        try {
            String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
            textArea.setText(content);
            textArea.setCaretPosition(0); // scroll to top
        } catch (IOException e) {
            OutputDisplay.error("Unable to load " + path, e);
        }
    }
}
