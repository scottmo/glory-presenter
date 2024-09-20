package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.*;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.scottmo.api.ConfigsController;
import com.scottmo.config.Config;
import com.scottmo.config.Labels;

public final class ConfigsTab extends JPanel {
    private ConfigsController controller = new ConfigsController();

    public ConfigsTab() {
        controller.setAppSize(Config.COMFORT_SIZE);

        JButton buttonToggleDarkMode = new JButton(Labels.get("configs.buttonToggleDarkMode"));
        buttonToggleDarkMode.addActionListener(evt -> controller.toggleDarkMode());

        JButton buttonChangeSize = new JButton(Labels.get("configs.buttonChangeSize"));
        buttonChangeSize.addActionListener(evt -> controller.toggleAppSize());

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(buttonToggleDarkMode),
            cell(buttonChangeSize)
        ).getComponent());
    }
}
