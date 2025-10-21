package com.scottmo.ui.containers;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.shared.Range;
import com.scottmo.shared.TextFormat;
import com.scottmo.ui.utils.Dialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.*;

public class FormatterTab extends JPanel {

    private static final String SAMPLE_FORMATS = """
fontFamily: KaiTi
fontSize: 12
fontColor: 255, 255, 255
italic: false
bold: false
underlined: false
strikethrough: false
""";

    private final ConfigService configService = ConfigService.get();
    private final PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();
    private final YAMLMapper yamlMapper = new YAMLMapper();

    private final JFileChooser fileChooser = new JFileChooser();
    private final JButton buttonFilePicker = new JButton(Labels.get("formatter.filePicker"));
    private final JSpinner inputStartSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JSpinner inputEndSlide = new JSpinner(new SpinnerNumberModel(5, 0, Integer.MAX_VALUE, 1));
    private final JTextField inputMatcher = new JTextField();
    private final JTextArea inputFormats = new JTextArea(5, 20);
    private final JButton buttonUpdate = new JButton(Labels.get("formatter.buttonUpdate"));

    private String targetFilePath;

    public FormatterTab() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("PowerPoint Files (*.pptx)", "pptx"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        buttonFilePicker.addActionListener(evt -> {
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                targetFilePath = selectedFile.getAbsolutePath();
                buttonFilePicker.setText(targetFilePath);
            }
        });

        buttonUpdate.addActionListener(evt -> {
            if (targetFilePath == null) {
                Dialog.error("No file selected!");
                return;
            }

            String textFormatsStr = inputFormats.getText();
            if (textFormatsStr == null || textFormatsStr.isEmpty()) {
                Dialog.error("No text formats specified!");
                return;
            }

            TextFormat textFormats;
            try {
                Map<String, TextFormat> textFormatPresets = configService.getConfig().getTextFormatPresets();
                textFormats = textFormatPresets.containsKey(textFormatsStr)
                    ? textFormatPresets.get(textFormatsStr)
                    : yamlMapper.readValue(textFormatsStr, TextFormat.class);
            } catch (Exception e) {
                Dialog.error("Invalid format: " + e.getMessage());
                return;
            }

            Pattern textMatchPattern;
            try {
                String inputMatcherText = inputMatcher.getText();
                Map<String, String> patternPresets = configService.getConfig().getPatternPresets();
                textMatchPattern = (patternPresets.containsKey(inputMatcherText))
                    ? Pattern.compile(patternPresets.get(inputMatcherText))
                    : Pattern.compile(inputMatcherText);
            } catch (PatternSyntaxException e) {
                Dialog.error("Invalid text pattern!");
                return;
            }

            try {
                int start = (Integer) inputStartSlide.getValue();
                int end = (Integer) inputEndSlide.getValue();
                powerpointService.updateTextFormats(targetFilePath, new Range(start, end), textMatchPattern, textFormats);
                Dialog.info("Update success!");
            } catch (IOException e) {
                Dialog.error("Failed to update file: " + e.getMessage());
            }
        });

        inputFormats.setText(SAMPLE_FORMATS);
        inputFormats.setLineWrap(true);
        inputFormats.setWrapStyleWord(true);

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(buttonFilePicker),
            row(UI_GAP,
                cell(new JLabel(Labels.get("formatter.rangeStart"))),
                cell(inputStartSlide).weightBy(1.0),
                cell(new JLabel(Labels.get("formatter.rangeEnd"))),
                cell(inputEndSlide).weightBy(1.0)
            ),
            cell(new JLabel(Labels.get("formatter.inputMatcher"))),
            cell(inputMatcher),
            cell(new JLabel(Labels.get("formatter.inputFormats"))),
            cell(new JScrollPane(inputFormats)),
            cell(buttonUpdate)
        ).getComponent());
    }
}
