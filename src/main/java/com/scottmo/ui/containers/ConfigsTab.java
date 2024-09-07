package com.scottmo.ui.containers;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.App;
import com.scottmo.config.Labels;
import com.scottmo.ui.components.Dialog;

public final class ConfigsTab extends JPanel {
    private boolean isDarkMode = true;

    public ConfigsTab() {
        JButton buttonToggleDarkMode = new JButton(Labels.get("configs.buttonToggleDarkMode"));
        buttonToggleDarkMode.addActionListener(evt -> toggleDarkMode());

        setLayout(new BorderLayout());
        add(buttonToggleDarkMode);
    }

    private void toggleDarkMode() {
        try {
            isDarkMode = !isDarkMode;
            UIManager.setLookAndFeel(isDarkMode ? new FlatDarkLaf() : new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(App.get()); 
        } catch (Exception e) {
            Dialog.error("Unable to toggle dark mode", e);
        }
    }
}
