package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;

public class ProgramTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

    private JTextArea fieldInput = new JTextArea(30, 30);
    private JButton buttonGeneratePPT = new JButton(Labels.get("program.buttonGeneratePPT"));
    private YAMLMapper yamlMapper = new YAMLMapper();

    public ProgramTab() {
        buttonGeneratePPT.addActionListener(evt -> {
            generatePPT(fieldInput.getText());
        });

        setLayout(new BorderLayout());
        add(row(UI_GAP,
            cell(fieldInput),
            cell(buttonGeneratePPT)
        ).getComponent());
    }

    private void generatePPT(String yamlConfigs) {
        try {
            List<PPTConfig> configs = yamlMapper.readValue(yamlConfigs,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, PPTConfig.class));
            List<String> tempFiles = new ArrayList<>();

            // Generate individual PPT files
            int i = 0;
            for (PPTConfig config : configs) {
                String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + config.type() + i + ".pptx";
                i++;
                String templatePath = configService.getRelativePath(config.template());
                switch (config.type().toLowerCase()) {
                    case "ppt":
                        List<Map<String, String>> values = yamlMapper.readValue(config.content(),
                            yamlMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        powerpointService.generate(values, templatePath, tempFilePath);
                        break;
                    case "song":
                        int songId = Integer.parseInt(config.content());
                        powerpointService.generate(songId, templatePath, tempFilePath, 2);
                        break;
                    case "bible":
                        String verses = config.content();
                        powerpointService.generate(verses, templatePath, tempFilePath);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown generator type: " + config.type());
                }
                tempFiles.add(tempFilePath);
            }
            powerpointService.mergeSlideShows(tempFiles, configService.getOutputPath("slidesShow.ppt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record PPTConfig(
        String type,
        String template,
        String content
    ) {}
}
