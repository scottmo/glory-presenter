package com.scottscmo.ui.container;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

public class CommandRunner extends JPanel {
    public CommandRunner() {
        super();

        this.setLayout(new MigLayout());

        JLabel inputLabel = new JLabel("Input:");
        JTextArea commandInput = new JTextArea();
        commandInput.setRows(3);
        JButton runBtn = new JButton("Run");
        JLabel outputLabel = new JLabel("Running from " + Path.of("").toAbsolutePath().toString() + ". Output:");
        JTextArea outputDisplay = new JTextArea();
        outputDisplay.setEditable(false);

        runBtn.addActionListener((ActionEvent actionEvent) -> {
            try {
                runCommand(commandInput.getText());
            } catch (Exception e) {
                outputDisplay.setForeground(Color.RED);
                outputDisplay.setText(e.getMessage());
            }
        });

        List<JComponent> components = List.of(
            inputLabel,
            commandInput,
            runBtn,
            outputDisplay,
            outputLabel
        );
        components.forEach(cmp -> this.add(cmp, "wrap"));
    }

    private void runCommand(String command) {

    }
}
