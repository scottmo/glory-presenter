package com.scottmo.ui.containers;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.shared.Range;
import com.scottmo.shared.TextFormat;
import com.scottmo.ui.utils.Dialog;
import org.httprpc.sierra.SuggestionPicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.scottmo.config.Config;

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
    private final JSpinner inputEndSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JTextArea inputFormats = new JTextArea(5, 20);
    private final SuggestionPicker inputMatcher = new SuggestionPicker(10);
    private final JButton buttonUpdate = new JButton(Labels.get("formatter.buttonUpdate"));
    private final JButton buttonNormalizeNewLine = new JButton(Labels.get("formatter.buttonNormalizeNewLine"));

    private List<String> targetFilePaths = new ArrayList<>();

    public FormatterTab() {
        Map<String, String> patternPresets = configService.getConfig().getPatternPresets();
        List<String> patternPresetNames = new ArrayList<>(patternPresets.keySet());
        inputMatcher.setText(patternPresetNames.get(0));
        inputMatcher.setSuggestions(patternPresetNames);

        fileChooser.setFileFilter(new FileNameExtensionFilter("PowerPoint Files (*.pptx)", "pptx"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        buttonFilePicker.addActionListener(evt -> {
            Path templateDir = Path.of(configService.getConfig().getDataDir(), Config.TEMPLATE_DIR);
            fileChooser.setCurrentDirectory(templateDir.toFile());
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = fileChooser.getSelectedFile();
                targetFilePaths.clear();
                if (selected.isDirectory()) {
                    File[] pptxFiles = selected.listFiles((dir, name) -> name.toLowerCase().endsWith(".pptx"));
                    if (pptxFiles != null) {
                        Arrays.stream(pptxFiles).map(File::getAbsolutePath).forEach(targetFilePaths::add);
                    }
                    buttonFilePicker.setText(selected.getAbsolutePath() + " (" + targetFilePaths.size() + " files)");
                } else {
                    targetFilePaths.add(selected.getAbsolutePath());
                    buttonFilePicker.setText(selected.getAbsolutePath());
                }
            }
        });

        buttonUpdate.addActionListener(evt -> {
            if (targetFilePaths.isEmpty()) {
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
                textMatchPattern = (patternPresets.containsKey(inputMatcherText))
                    ? Pattern.compile(patternPresets.get(inputMatcherText))
                    : Pattern.compile(inputMatcherText);
            } catch (PatternSyntaxException e) {
                Dialog.error("Invalid text pattern!");
                return;
            }

            List<String> errors = new ArrayList<>();
            int start = (Integer) inputStartSlide.getValue();
            int end = (Integer) inputEndSlide.getValue();
            for (String filePath : targetFilePaths) {
                try {
                    powerpointService.updateTextFormats(filePath, getOutputPath(filePath), new Range(start, end), textMatchPattern, textFormats);
                } catch (IOException e) {
                    errors.add(filePath + ": " + e.getMessage());
                }
            }
            if (errors.isEmpty()) {
                Dialog.info("Updated " + targetFilePaths.size() + " file(s) successfully!");
            } else {
                Dialog.error("Failed to update " + errors.size() + " file(s):\n" + String.join("\n", errors));
            }
        });

        buttonNormalizeNewLine.addActionListener(evt -> {
            if (targetFilePaths.isEmpty()) {
                Dialog.error("No file selected!");
                return;
            }

            List<String> errors = new ArrayList<>();
            for (String filePath : targetFilePaths) {
                try {
                    powerpointService.normalizeNewLines(filePath, getOutputPath(filePath));
                } catch (IOException e) {
                    errors.add(filePath + ": " + e.getMessage());
                }
            }
            if (errors.isEmpty()) {
                Dialog.info("Updated " + targetFilePaths.size() + " file(s) successfully!");
            } else {
                Dialog.error("Failed to update " + errors.size() + " file(s):\n" + String.join("\n", errors));
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
            cell(buttonUpdate),
            cell(buttonNormalizeNewLine)
        ).getComponent());
    }

    private String getOutputPath(String filePath) {
        return filePath.replace(".pptx", ".mod.pptx");
    }
}
