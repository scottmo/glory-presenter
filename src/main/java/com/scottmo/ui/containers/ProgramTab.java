package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.nio.file.Path;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.FilePicker;

public class ProgramTab extends JPanel {

    private static final String SAMPLE_INPUT = """
- type: default
  template: default.pptx
  content: |
    - metadata: none
    - title_zh: 序樂
      title_en: Prelude
    - title_zh: 宣召
      title_en: Call to Worship
- type: bible
  template: bible-en-only.pptx
  content: psalms 51:10-14
- type: bible
  content: john 1:1;mark 3:1
- type: song
  template: song.pptx
  content: |
    linesPerSlide: 2
    songId: 321
""";

    private ConfigService configService = ConfigService.get();
    private BibleService bibleService = ServiceProvider.get(BibleService.class).get();
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

    private JComboBox<String> bookComboBox = new JComboBox<>();
    private JButton buttonPickFile = new JButton("Pick Template File");
    private JTextArea fieldInput = new JTextArea(SAMPLE_INPUT, 30, 30);
    private JButton buttonGeneratePPT = new JButton(Labels.get("program.buttonGeneratePPT"));

    public ProgramTab() {
        bookComboBox.addItem("Select Bible Book...");
        bibleService.getBooks().forEach(bookComboBox::addItem);
        bookComboBox.addActionListener(evt -> {
            String selectedBook = (String) bookComboBox.getSelectedItem();
            if (selectedBook != null && !selectedBook.equals("Select Bible Book...")) {
                insertBibleBlock(selectedBook);
                bookComboBox.setSelectedIndex(0);
            }
        });

        buttonPickFile.addActionListener(evt -> {
            Path templateDir = Path.of(configService.getConfig().getDataDir(), Config.TEMPLATE_DIR);
            FilePicker.show(FilePicker.FILES_AND_DIRECTORIES, templateDir.toString(), selectedPath -> {
                String relativePath = getRelativeTemplatePath(selectedPath);
                if (relativePath != null) {
                    insertTextAtCursor(relativePath);
                }
            });
        });

        buttonGeneratePPT.addActionListener(evt -> {
            try {
                String outPath = configService.getOutputPath("output.pptx");
                powerpointService.generateFromYamlConfigs(fieldInput.getText(), outPath);
                Dialog.info("Generated slides at " + outPath);
            } catch (IOException e) {
                Dialog.error("Error generating program slides", e); 
            }
        });

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            row(UI_GAP,
                cell(bookComboBox),
                cell(buttonPickFile)
            ),
            cell(new JScrollPane(fieldInput)).with(scrollPane -> 
                fieldInput.setFont(new Font(Font.MONOSPACED, Font.BOLD, configService.getConfig().getAppSize().font()))).weightBy(1),
            cell(buttonGeneratePPT)
        ).getComponent());
    }

    private void insertBibleBlock(String bookName) {
        String text = fieldInput.getText();
        int cursorPos = fieldInput.getCaretPosition();
        
        int nextNewline = text.indexOf('\n', cursorPos);
        int insertPos = (nextNewline == -1) ? text.length() : nextNewline + 1;
        
        String bibleTemplate = configService.getConfig().getDefaultTemplates().get("bible");
        String bibleBlock = "- type: bible\n  template: " + bibleTemplate + "\n  content: " + bookName;
        if (insertPos > 0 && text.charAt(insertPos - 1) != '\n') {
            bibleBlock = "\n" + bibleBlock;
        }
        if (insertPos < text.length() && text.charAt(insertPos) != '\n' && text.charAt(insertPos) != '\r') {
            bibleBlock = bibleBlock + "\n";
        }
        
        String newText = text.substring(0, insertPos) + bibleBlock + text.substring(insertPos);
        fieldInput.setText(newText);
        fieldInput.setCaretPosition(insertPos + bibleBlock.length());
    }

    private String getRelativeTemplatePath(String filePath) {
        try {
            Path templateDir = Path.of(configService.getConfig().getDataDir(), Config.TEMPLATE_DIR).toAbsolutePath().normalize();
            Path selectedPath = Path.of(filePath).toAbsolutePath().normalize();
            
            if (selectedPath.startsWith(templateDir)) {
                Path relativePath = templateDir.relativize(selectedPath);
                return relativePath.toString().replace('\\', '/');
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void insertTextAtCursor(String text) {
        String content = fieldInput.getText();
        int cursorPos = fieldInput.getCaretPosition();
        String newText = content.substring(0, cursorPos) + text + content.substring(cursorPos);
        fieldInput.setText(newText);
        fieldInput.setCaretPosition(cursorPos + text.length());
    }
}
