package com.scottscmo.ui.container;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import com.scottscmo.config.Config;

public class DataPathPicker {
    private static final String BUTTON_TEXT_PREFIX = "Data Path: ";

    public static JComponent create(Component host) {
        Path dataPath = Path.of(Config.get(Config.DIR_DATA));

        JButton setDataPathBtn = new JButton(BUTTON_TEXT_PREFIX + dataPath.toString());
        setDataPathBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(dataPath.toFile()); // start at application current directory
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showSaveDialog(host);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String newDataPath = fc.getSelectedFile().toPath().toAbsolutePath().toString();
                setDataPathBtn.setText(BUTTON_TEXT_PREFIX + newDataPath);
                Config.set(Config.DIR_DATA, newDataPath);
            }
        });
        return setDataPathBtn;
    }
}
