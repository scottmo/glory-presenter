package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.scottmo.config.Labels;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.ui.utils.Dialog;

public class ProgramTab extends JPanel {
    private PowerpointService powerpointService = ServiceProvider.get(PowerpointService.class).get();

    private JTextArea fieldInput = new JTextArea(30, 30);
    private JButton buttonGeneratePPT = new JButton(Labels.get("program.buttonGeneratePPT"));

    public ProgramTab() {
        buttonGeneratePPT.addActionListener(evt -> {
            try {
                powerpointService.generateFromYamlConfigs(fieldInput.getText());
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
