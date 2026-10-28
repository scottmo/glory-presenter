package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.operations.FormatterOperations;
import com.scottmo.ui.utils.Dialog;

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
    private final FormatterOperations formatterOperations = FormatterOperations.forApplication();
    private final JFileChooser fileChooser = new JFileChooser();
    private final JButton buttonFilePicker = new JButton(Labels.get("formatter.filePicker"));
    private final JSpinner inputStartSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JSpinner inputEndSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JTextArea inputFormats = new JTextArea(10, 20);
    private final SuggestionPicker inputMatcher = new SuggestionPicker(10);
    private final JCheckBox inputOverwrite = new JCheckBox(Labels.get("formatter.checkboxOverwrite"));
    private final JButton buttonUpdate = new JButton(Labels.get("formatter.buttonUpdate"));
    private final JButton buttonNormalizeNewLine = new JButton(Labels.get("formatter.buttonNormalizeNewLine"));

    private List<String> targetFilePaths = new ArrayList<>();

    public FormatterTab() {
        List<String> patternPresetNames = formatterOperations.listPatternPresetNames();
        inputMatcher.setText(patternPresetNames.isEmpty() ? "" : patternPresetNames.get(0));
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
                try {
                    List<Path> selectedFiles = formatterOperations.collectFiles(List.of(selected.getAbsolutePath()));
                    targetFilePaths = selectedFiles.stream().map(Path::toString).toList();
                    buttonFilePicker.setText(selected.getAbsolutePath() + (selected.isDirectory()
                        ? " (" + targetFilePaths.size() + " files)"
                        : ""));
                } catch (Exception e) {
                    Dialog.error("Unable to select PowerPoint files: " + e.getMessage());
                }
            }
        });

        buttonUpdate.addActionListener(evt -> {
            try {
                FormatterOperations.BatchResult result = formatterOperations.formatText(
                    targetFilePaths,
                    (Integer) inputStartSlide.getValue(),
                    (Integer) inputEndSlide.getValue(),
                    inputMatcher.getText(),
                    inputFormats.getText(),
                    inputOverwrite.isSelected()
                );
                showResult("text formatting", result);
            } catch (IllegalArgumentException | IOException e) {
                Dialog.error("Unable to format PowerPoint text: " + e.getMessage());
            }
        });

        buttonNormalizeNewLine.addActionListener(evt -> {
            try {
                FormatterOperations.BatchResult result = formatterOperations.normalizeNewlines(
                    targetFilePaths, inputOverwrite.isSelected());
                showResult("newline normalization", result);
            } catch (IllegalArgumentException | IOException e) {
                Dialog.error("Unable to normalize PowerPoint newlines: " + e.getMessage());
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
            cell(inputOverwrite),
            cell(buttonUpdate),
            cell(buttonNormalizeNewLine)
        ).getComponent());
    }

    private void showResult(String operation, FormatterOperations.BatchResult result) {
        if (result.failures().isEmpty()) {
            Dialog.info("Completed " + operation + " for " + result.successes().size() + " file(s).");
            return;
        }
        String failures = result.failures().stream()
            .map(failure -> failure.inputPath() + ": " + failure.error())
            .collect(java.util.stream.Collectors.joining("\n"));
        Dialog.error("Completed " + operation + " for " + result.successes().size()
            + " file(s); failed for " + result.failures().size() + ":\n" + failures);
    }
}
