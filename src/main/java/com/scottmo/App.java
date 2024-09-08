package com.scottmo;

import java.awt.KeyboardFocusManager;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.httprpc.sierra.ScrollingKeyboardFocusManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.ui.components.Dialog;
import com.scottmo.ui.containers.BibleTab;
import com.scottmo.ui.containers.ConfigsTab;
import com.scottmo.ui.containers.SongTab;

public class App extends JFrame {
    ConfigService configService = ConfigService.get();

    private static App INSTANCE;
    public static App get() {
        return INSTANCE;
    }

    public App() {
        setTitle(Labels.get("appName"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(Config.APP_WIDTH, Config.APP_HEIGHT);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(Labels.get("songs.containerTitle"), new SongTab());
        tabs.addTab(Labels.get("bible.containerTitle"), new BibleTab());
        tabs.addTab(Labels.get("configs.containerTitle"), new ConfigsTab());
        getContentPane().add(tabs);
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new ScrollingKeyboardFocusManager());
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                Dialog.error(String.format("[Error] %s (see error.log for details)", e.getMessage()), e);
            });

            INSTANCE = new App();
            INSTANCE.setVisible(true);
        });
    }
}
