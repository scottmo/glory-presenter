package com.scottmo.ui.utils;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.App;

public class UiConfigurator {
    private boolean isDarkMode = true;
    public void toggleDarkMode() {
        try {
            isDarkMode = !isDarkMode;
            UIManager.setLookAndFeel(isDarkMode ? new FlatDarkLaf() : new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(App.get()); 
        } catch (Exception e) {
            Dialog.error("Unable to toggle dark mode", e);
        }
    }
}
