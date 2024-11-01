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

import com.scottmo.api.SongController;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.SongService;

public class ProgramTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();
    private SongController songController = new SongController(
        ServiceProvider.get(SongService.class).get(),
        powerpointService);

    private JTextArea fieldInput = new JTextArea(30, 30);
    private JButton buttonGeneratePPT = new JButton(Labels.get("program.buttonGeneratePPT"));

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
            var mapper = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper();
            List<PPTConfig> configs = mapper.readValue(yamlConfigs, mapper.getTypeFactory().constructCollectionType(List.class, PPTConfig.class));
            List<String> tempFiles = new ArrayList<>();

            // Generate individual PPT files
            int i = 0;
            for (PPTConfig config : configs) {
                String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + config.type() + i + ".pptx";
                i++;
                String templatePath = configService.getRelativePath(config.path());
                switch (config.type().toLowerCase()) {
                    case "ppt":
                        List<Map<String, String>> values = config.values() == null ? List.of() : List.of(config.values());
                        powerpointService.generate(values, templatePath, tempFilePath, false, false);
                        break;
                    case "song":
                        tempFilePath = songController.generatePPTX(config.id(), 2, templatePath);
                        break;
                    case "bible":
                        powerpointService.generate(config.verses(), templatePath, tempFilePath);
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
        String path,
        Map<String, String> values,
        String verses,
        Integer id
    ) {}
}
