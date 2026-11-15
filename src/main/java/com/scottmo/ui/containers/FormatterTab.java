package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.operations.FormatterOperations;
import com.scottmo.core.operations.FormatterOperations.BatchResult;
import com.scottmo.core.operations.FormatterOperations.DirectoryResult;
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
    private final JButton buttonDirectoryPicker = new JButton(Labels.get("formatter.directoryPicker"));
    private final JTextField selectedPath = new JTextField(20);
    private final JSpinner inputStartSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JSpinner inputEndSlide = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JTextArea inputFormats = new JTextArea(10, 20);
    private final SuggestionPicker inputMatcher = new SuggestionPicker(10);
    private final JCheckBox inputOverwrite = new JCheckBox(Labels.get("formatter.checkboxOverwrite"));
    private final JButton buttonUpdate = new JButton(Labels.get("formatter.buttonUpdate"));
    private final JButton buttonNormalizeNewLine = new JButton(Labels.get("formatter.buttonNormalizeNewLine"));
    private final JLabel status = new JLabel();

    private String targetPath;
    private boolean directorySelected;

    public FormatterTab() {
        List<String> patternPresetNames = formatterOperations.listPatternPresetNames();
        inputMatcher.setText(patternPresetNames.isEmpty() ? "" : patternPresetNames.get(0));
        inputMatcher.setSuggestions(patternPresetNames);

        fileChooser.setFileFilter(new FileNameExtensionFilter("PowerPoint Files (*.pptx)", "pptx"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        buttonFilePicker.addActionListener(evt -> selectTargets(JFileChooser.FILES_ONLY));
        buttonDirectoryPicker.addActionListener(evt -> selectTargets(JFileChooser.DIRECTORIES_ONLY));
        selectedPath.setEditable(false);
        selectedPath.getAccessibleContext().setAccessibleName(Labels.get("formatter.selectedPath"));
        buttonUpdate.addActionListener(evt -> formatSelection());
        buttonNormalizeNewLine.addActionListener(evt -> normalizeSelection());
        updateControls(false);

        inputFormats.setText(SAMPLE_FORMATS);
        inputFormats.setLineWrap(true);
        inputFormats.setWrapStyleWord(true);

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            row(UI_GAP, cell(buttonFilePicker), cell(buttonDirectoryPicker)),
            cell(selectedPath),
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
            row(UI_GAP, cell(inputOverwrite), cell(status).weightBy(1.0)),
            cell(buttonUpdate),
            cell(buttonNormalizeNewLine)
        ).getComponent());
    }

    private void formatSelection() {
        String path = targetPath;
        int startSlide = (Integer) inputStartSlide.getValue();
        int endSlide = (Integer) inputEndSlide.getValue();
        String matcher = inputMatcher.getText();
        String format = inputFormats.getText();
        boolean overwrite = inputOverwrite.isSelected();
        if (directorySelected) {
            runOperation("text formatting", () -> {
                DirectoryResult result = formatterOperations.formatDirectory(path, startSlide, endSlide, matcher, format);
                return new OperationResult(result.result(), result.backupDirectory());
            });
        } else {
            runOperation("text formatting", () -> new OperationResult(formatterOperations.formatText(
                List.of(path), startSlide, endSlide, matcher, format, overwrite), null));
        }
    }

    private void normalizeSelection() {
        String path = targetPath;
        boolean overwrite = inputOverwrite.isSelected();
        if (directorySelected) {
            runOperation("newline normalization", () -> {
                DirectoryResult result = formatterOperations.normalizeDirectory(path);
                return new OperationResult(result.result(), result.backupDirectory());
            });
        } else {
            runOperation("newline normalization", () -> new OperationResult(
                formatterOperations.normalizeNewlines(List.of(path), overwrite), null));
        }
    }

    private void runOperation(String operation, Callable<OperationResult> task) {
        updateControls(true);
        new SwingWorker<OperationResult, Void>() {
            @Override
            protected OperationResult doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                updateControls(false);
                try {
                    showResult(operation, get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Dialog.error("Unable to complete " + operation + ". " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void updateControls(boolean running) {
        boolean ready = !running && targetPath != null;
        buttonFilePicker.setEnabled(!running);
        buttonDirectoryPicker.setEnabled(!running);
        inputOverwrite.setVisible(!directorySelected);
        inputOverwrite.setEnabled(!running && !directorySelected);
        buttonUpdate.setEnabled(ready);
        buttonNormalizeNewLine.setEnabled(ready);
        status.setText(running ? Labels.get("formatter.working")
            : directorySelected ? Labels.get("formatter.directoryBackup") : "");
    }

    private void showResult(String operation, OperationResult outcome) {
        BatchResult result = outcome.batch();
        String summary = "Completed " + operation + " for " + result.successes().size() + " file(s).";
        String backupMessage = outcome.backupDirectory() == null ? ""
            : "\nBackup folder\n" + outcome.backupDirectory();
        if (result.failures().isEmpty()) {
            Dialog.info(summary + backupMessage);
            return;
        }
        String failures = result.failures().stream()
            .map(failure -> failure.inputPath() + ": " + failure.error())
            .collect(Collectors.joining("\n"));
        Dialog.error(summary + " Failed for " + result.failures().size() + " file(s)."
            + backupMessage + "\n\n" + failures);
    }

    private void selectTargets(int selectionMode) {
        Path templateDir = Path.of(configService.getConfig().getDataDir(), Config.TEMPLATE_DIR);
        fileChooser.setCurrentDirectory(templateDir.toFile());
        fileChooser.setFileSelectionMode(selectionMode);
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = fileChooser.getSelectedFile();
        if (selected == null) {
            return;
        }
        targetPath = selected.getAbsolutePath();
        directorySelected = selectionMode == JFileChooser.DIRECTORIES_ONLY;
        selectedPath.setText(targetPath);
        selectedPath.setToolTipText(targetPath);
        updateControls(false);
    }

    private record OperationResult(BatchResult batch, String backupDirectory) {}
}
