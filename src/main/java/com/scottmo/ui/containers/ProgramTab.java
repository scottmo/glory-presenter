package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.row;

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

/*
Sample program template:
- type: default
  template: /templates/gcbc-start.pptx
- type: default
  template: /templates/gcbc-title.pptx
  content: |
    - metadata: none
    - title_zh: 序樂
      title_en: Prelude
    - title_zh: 宣召
      title_en: Call to Worship
- type: bible
  template: /templates/gcbc-bible.pptx
  content: psalms 51:10-14
- type: song
  template: /templates/gcbc-song.pptx
  content: 321
 */

public class ProgramTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

    private JTextArea fieldInput = new JTextArea(30, 30);
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
        add(row(UI_GAP,
            cell(fieldInput),
            cell(buttonGeneratePPT)
        ).getComponent());
    }
}
