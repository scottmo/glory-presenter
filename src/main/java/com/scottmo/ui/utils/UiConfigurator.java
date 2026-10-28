package com.scottmo.ui.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.scottmo.App;
import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;

public class UiConfigurator {
    private static volatile boolean isDarkMode = true;

    public static void toggleDarkMode() {
        boolean darkMode = !ConfigService.get().getConfig().isDarkMode();
        com.scottmo.core.operations.ConfigOperations.forApplication().setDarkMode(darkMode);
    }

    public static void initializeTheme(boolean darkMode) {
        isDarkMode = darkMode;
        try {
            if (darkMode) {
                FlatCarbonIJTheme.setup();
            } else {
                FlatCyanLightIJTheme.setup();
            }
        } catch (Exception e) {
            Dialog.error("Unable to initialize theme", e);
        }
    }

    public static void applyConfiguration(Config config) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(() -> applyConfigurationNow(config));
    }

    public static void applyTheme(boolean darkMode) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        isDarkMode = darkMode;
        SwingUtilities.invokeLater(() -> {
            if (applyLookAndFeel(darkMode, "Unable to change theme")) {
                App app = App.get();
                if (app != null) {
                    SwingUtilities.updateComponentTreeUI(app);
                }
            }
        });
    }

    private static void applyConfigurationNow(Config config) {
        System.setProperty("flatlaf.uiScale", config.getUiScale());
        isDarkMode = config.isDarkMode();
        if (!applyLookAndFeel(isDarkMode, "Unable to apply configuration")) {
            return;
        }
        initializeGlobalFont(config);
        App app = App.get();
        if (app != null) {
            SwingUtilities.updateComponentTreeUI(app);
            Config.AppSize size = config.getAppSize();
            app.setSize(size.width(), size.height());
        }
    }

    private static boolean applyLookAndFeel(boolean darkMode, String errorTitle) {
        try {
            UIManager.setLookAndFeel(darkMode ? new FlatCarbonIJTheme() : new FlatCyanLightIJTheme());
            return true;
        } catch (Exception e) {
            Dialog.error(errorTitle, e);
            return false;
        }
    }

    public static void initializeGlobalFont() {
        initializeGlobalFont(ConfigService.get().getConfig());
    }

    public static void initializeGlobalFont(Config config) {
        String uiFontFamily = config.getUiFontFamily();
        int fontSize = config.getAppFontSize();

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
