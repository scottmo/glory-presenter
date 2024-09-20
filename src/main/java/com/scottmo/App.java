package com.scottmo;

import static com.scottmo.config.Config.UI_GAP;

import java.awt.KeyboardFocusManager;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.httprpc.sierra.ScrollingKeyboardFocusManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.scottmo.api.ConfigsController;
import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.shared.Pair;
import com.scottmo.ui.components.Dialog;
import com.scottmo.ui.containers.BibleTab;
import com.scottmo.ui.containers.ConfigsTab;
import com.scottmo.ui.containers.SongTab;

public class App extends JFrame {
    private static final int MARGIN = UI_GAP * 2;

    ConfigService configService = ConfigService.get();

    private static App INSTANCE;
    public static App get() {
        return INSTANCE;
    }

    public App() {
        setTitle(Labels.get("appName"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void render() {
        JTabbedPane tabs = new JTabbedPane();
        Pair.<String, JPanel>ofList(
            "songs.containerTitle", new SongTab(),
            "bible.containerTitle", new BibleTab(),
            "configs.containerTitle", new ConfigsTab()
        ).forEach(tabCmp -> {
            tabCmp.value().setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
            tabs.addTab(Labels.get(tabCmp.key()), tabCmp.value());
        });
        getContentPane().add(tabs);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new ScrollingKeyboardFocusManager());
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                Dialog.error(String.format("[Error] %s (see error.log for details)", e.getMessage()), e);
            });

            INSTANCE = new App();
            INSTANCE.render();
        });
    }
}
