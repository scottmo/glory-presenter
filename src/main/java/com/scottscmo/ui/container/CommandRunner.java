package com.scottscmo.ui.container;

import java.awt.Color;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

public class CommandRunner extends JPanel {

    public CommandRunner() {
        super();

        // components init

        JLabel inputLabel = new JLabel("Input:");
        JTextArea commandInput = new JTextArea();
        JButton runBtn = new JButton("Run");
        JLabel outputLabel = new JLabel("Output:");
        JTextArea outputDisplay = new JTextArea();

        // components config

        commandInput.setRows(3);

        outputDisplay.setEditable(false);

        runBtn.addActionListener(actionEvent -> {
            try {
                runCommand(commandInput.getText());
            } catch (Exception e) {
                outputDisplay.setForeground(Color.RED);
                outputDisplay.setText(e.getMessage());
            }
        });

        // layout

        this.setLayout(new MigLayout());
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
