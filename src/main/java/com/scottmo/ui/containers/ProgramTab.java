package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.ui.utils.Dialog;

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
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

    private JTextArea fieldInput = new JTextArea(SAMPLE_INPUT, 30, 30);
    private JButton buttonGeneratePPT = new JButton(Labels.get("program.buttonGeneratePPT"));

    public ProgramTab() {
        buttonGeneratePPT.addActionListener(evt -> {
            try {
                String outPath = configService.getOutputPath("slidesShow.ppt");
                powerpointService.generateFromYamlConfigs(fieldInput.getText(), outPath);
                Dialog.info("Generated slides at " + outPath);
            } catch (IOException e) {
                Dialog.error("Error generating program slides", e); 
            }
        });

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(fieldInput),
            cell(buttonGeneratePPT)
        ).getComponent());
    }
}
