package com.scottmo.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class Form {
    private static final Map<String, Integer> FILE_PICKER_TYPE_MAP = Map.of(
            "file", FilePicker.FILES,
            "directory", FilePicker.DIRECTORIES,
            "fileAndDirectory", FilePicker.FILES_AND_DIRECTORIES,
            "fileSearch", FilePicker.FILE_SEARCH
    );

    private Map<String, Component> inputs = new HashMap<>();

    private final JPanel ui = new JPanel();

    public Form(String title, List<FormInput> inputConfigs, Function<Form, String> onSubmit) {
        JLabel titleLabel = new JLabel(title);
        JButton submitBtn = new JButton("Submit");

        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, titleLabel.getFont().getSize() + 2));

        ui.setMinimumSize(new Dimension(400, 0));
        // ui.setLayout(new MigLayout("ins 0, wrap 2", "[100][100, left, fill, grow]"));
        ui.add(titleLabel, "span");

        for (var config : inputConfigs) {
            ui.add(new JLabel(config.label()));

            JComponent input = buildInput(config);
            inputs.put(config.id(), input);
            ui.add(input);
        }

        ui.add(submitBtn, "skip, tag apply");

        // controls
        submitBtn.addActionListener(e -> {
            String result = onSubmit.apply(this);
            if (result != null) {
                Dialog.info(result);
            }
        });
    }

    private JComponent buildInput(FormInput config) {
        if (FILE_PICKER_TYPE_MAP.containsKey(config.type())) {
            var input = new JTextField(config.defaultValue());
            input.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent me) {
                    FilePicker.show(FILE_PICKER_TYPE_MAP.get(config.type()), config.defaultValue(), input::setText);
                }
            });
            return input;
        } else if ("textarea".equals(config.type())) {
            var input = new JTextArea(config.height(), config.width());
            // input.setFont(configService.getConfig().getTextAreaFont());
            return input;
        } else {
            return new JTextField(config.defaultValue());
        }
    }

    public JComponent getUI() {
        return ui;
    }

    public String getValue(String key) {
        if (inputs.containsKey(key)) {
            Component component = inputs.get(key);
            if (component instanceof JTextField) {
                return ((JTextField) component).getText();
            } else if (component instanceof JTextArea) {
                return ((JTextArea) component).getText();
            }
        }
        return "";
    }
}
