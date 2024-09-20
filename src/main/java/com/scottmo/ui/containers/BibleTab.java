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

import com.scottmo.api.BibleController;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.shared.StringUtils;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.FilePicker;

public final class BibleTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private BibleController controller = new BibleController();

    private JButton buttonImport = new JButton(Labels.get("bible.buttonImport"));
    private JButton buttonGenerateGSlide = new JButton(Labels.get("bible.buttonGenerateGSlide"));
    private JButton buttonGeneratePPT = new JButton(Labels.get("bible.buttonGeneratePPT"));
    private SuggestionPicker inputTemplate = new SuggestionPicker(10);
    private JTextField inputBibleRef = new JTextField();

    public BibleTab() {
        Consumer<JComponent> indentation = cmp -> cmp.setBorder(new EmptyBorder(0, 20, 0, 0));

        buttonGeneratePPT.addActionListener(evt -> {
            try {
                controller.generatePPTX(inputBibleRef.getText(), inputTemplate.getText());
            } catch (Exception e) {
                Dialog.error("Unable to generate pptx for " + inputBibleRef.getText(), e);
                e.printStackTrace();
            }
        });

        buttonImport.addActionListener(evt -> {
            FilePicker.show(selectedFilePath -> {
                try {
                    controller.importBibles(List.of(selectedFilePath));
                    Dialog.info("Import success! Please restart for it to be loaded!");
                } catch (Exception e) {
                    Dialog.error("Error importing " + selectedFilePath, e);
                }
            });
        });

        List<String> templatePaths = new ArrayList<>(configService.getConfig().getTemplatePaths());
        inputTemplate.setText(templatePaths.stream().filter(path -> path.toLowerCase().contains("bible")).findFirst().orElse(""));
        inputTemplate.setSuggestions(templatePaths);
 
        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(namedLabel("bible.availableVersions")),
            cell(label(StringUtils.join(controller.getVersions()))).with(indentation),
            cell(namedLabel("bible.bookKeys")),
            cell(label(StringUtils.join(controller.getBooks()))).with(indentation),
            cell(new JSeparator()),
            cell(namedLabel("bible.inputBibleRef")),
            cell(inputBibleRef),
            cell(namedLabel("bible.inputTemplate")),
            cell(inputTemplate),
            cell(buttonGeneratePPT),
            cell(buttonGenerateGSlide),
            cell(new JSeparator()),
            cell(buttonImport)
        ).getComponent());
    }
}
