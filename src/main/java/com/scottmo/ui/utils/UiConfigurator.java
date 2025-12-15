package com.scottmo.ui.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.App;
import com.scottmo.config.ConfigService;

public class UiConfigurator {
    private static boolean isDarkMode = true;

    public static void toggleDarkMode() {
        try {
            isDarkMode = !isDarkMode;
            UIManager.setLookAndFeel(isDarkMode ? new FlatDarkLaf() : new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(App.get()); 
        } catch (Exception e) {
            Dialog.error("Unable to toggle dark mode", e);
        }
    }

    public static void initializeGlobalFont() {
        String uiFontFamily = ConfigService.get().getConfig().getUiFontFamily();
        int fontSize = ConfigService.get().getConfig().getAppFontSize();

        if (uiFontFamily != null && !uiFontFamily.isEmpty()) {
            Set<String> availableFonts = Set.of(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            for (String fontName : uiFontFamily.split(",")) {
                String trimmed = fontName.trim();
                if (availableFonts.contains(trimmed)) {
                    setGlobalFont(new Font(trimmed, Font.PLAIN, fontSize));
                    break;
                }
            }
        }
    }

    private static void setGlobalFont(Font font) {
        FontUIResource fontResource = new FontUIResource(font);

        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontResource);
            }
        }
    }
}
