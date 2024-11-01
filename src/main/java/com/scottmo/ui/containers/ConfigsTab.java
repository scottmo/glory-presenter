package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.scottmo.config.Config;
import com.scottmo.config.Labels;
import com.scottmo.ui.utils.UiConfigurator;

public final class ConfigsTab extends JPanel {
    private UiConfigurator uiConfigurator = new UiConfigurator();

    public ConfigsTab() {
        uiConfigurator.setAppSize(Config.COMFORT_SIZE);

        JButton buttonToggleDarkMode = new JButton(Labels.get("configs.buttonToggleDarkMode"));
        buttonToggleDarkMode.addActionListener(evt -> uiConfigurator.toggleDarkMode());

        JButton buttonChangeSize = new JButton(Labels.get("configs.buttonChangeSize"));
        buttonChangeSize.addActionListener(evt -> uiConfigurator.toggleAppSize());

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(buttonToggleDarkMode),
            cell(buttonChangeSize)
        ).getComponent());
    }
}
