package com.scottmo.ui.utils;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.App;
import com.scottmo.config.Config;
import com.scottmo.config.Config.AppSize;
import com.scottmo.config.ConfigService;

public class UiConfigurator {
    private ConfigService configService = ConfigService.get();

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

    public void toggleAppSize() {
        AppSize newAppSize = configService.getConfig().getAppSize().equals(Config.COMPACT_SIZE)
            ? Config.COMFORT_SIZE
            : Config.COMPACT_SIZE;
        setAppSize(newAppSize);
    }

    public void setAppSize(Config.AppSize appSize) {
        SwingUtilities.invokeLater(() -> {
            configService.getConfig().setAppSize(appSize);
            setUIFontSize(appSize.font());
            App.get().setSize(appSize.width(), appSize.height());
            SwingUtilities.updateComponentTreeUI(App.get());
        });
    }

    private void setUIFontSize(int newSize) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value != null && value instanceof FontUIResource) {
                FontUIResource oldFont = (FontUIResource) value;
                // Default font may not support CJK characters, so this creates a composite font
                // that fallbacks to a different font for rendering CJK characters instead of squares.
                Font compositeNewFont = StyleContext.getDefaultStyleContext()
                        .getFont(oldFont.getFamily(), oldFont.getStyle(), newSize);
                // Need to use FontUIResource instead of Font otherwise the font cannot be changed
                // multiple times during runtime.
                FontUIResource newFont = new FontUIResource(compositeNewFont);
                
                UIManager.put(key, newFont);
            }
        }
    }
}
