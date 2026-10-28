package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static com.scottmo.ui.utils.ComponentBuilder.label;
import static com.scottmo.ui.utils.ComponentBuilder.namedLabel;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.httprpc.sierra.SuggestionPicker;

import com.scottmo.config.Labels;
import com.scottmo.core.operations.BibleOperations;
import com.scottmo.shared.StringUtils;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.FilePicker;

public final class BibleTab extends JPanel {
    private final BibleOperations bibleOperations = BibleOperations.forApplication();

    private JButton buttonImport = new JButton(Labels.get("bible.buttonImport"));
    private JButton buttonGeneratePPT = new JButton(Labels.get("bible.buttonGeneratePPT"));
    private SuggestionPicker inputTemplate = new SuggestionPicker(10);
    private JTextField inputBibleRef = new JTextField();

    public BibleTab() {
        Consumer<JComponent> indentation = cmp -> cmp.setBorder(new EmptyBorder(0, 20, 0, 0));

        buttonGeneratePPT.addActionListener(evt -> {
            try {
                String outputPath = bibleOperations.generatePowerpoint(
                    inputBibleRef.getText(), inputTemplate.getText(), null);
                Dialog.info("Generation success! Slides generated at " + outputPath);
            } catch (Exception e) {
                Dialog.error("Unable to generate pptx for " + inputBibleRef.getText(), e);
                e.printStackTrace();
            }
        });

        buttonImport.addActionListener(evt -> {
            FilePicker.show(selectedFilePath -> {
                try {
                    var result = bibleOperations.importOsis(List.of(selectedFilePath));
                    if (result.failures().isEmpty()) {
                        Dialog.info("Import success!");
                    } else {
                        Dialog.error("Bible import failed: " + result.failures());
                    }
                } catch (Exception e) {
                    Dialog.error("Error importing " + selectedFilePath, e);
                }
            });
        });

        List<String> templatePaths = new ArrayList<>(bibleOperations.listConfiguredTemplates());
        inputTemplate.setText(templatePaths.stream().filter(path -> path.toLowerCase().contains("bible")).findFirst().orElse(""));
        inputTemplate.setSuggestions(templatePaths);
 
        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(namedLabel("bible.availableVersions")),
            cell(label(StringUtils.join(bibleOperations.listVersions()))).with(indentation),
            cell(namedLabel("bible.bookKeys")),
            cell(label(StringUtils.join(bibleOperations.listBooks()))).with(indentation),
            cell(new JSeparator()),
            cell(namedLabel("bible.inputBibleRef")),
            cell(inputBibleRef),
            cell(namedLabel("bible.inputTemplate")),
            cell(inputTemplate),
            cell(buttonGeneratePPT),
            cell(new JSeparator()),
            cell(buttonImport)
        ).getComponent());
    }

}
