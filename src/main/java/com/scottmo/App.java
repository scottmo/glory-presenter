package com.scottmo;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.ui.panels.PPTXGeneratorsPanel;
import com.scottmo.ui.panels.SettingsPanel;
import com.scottmo.ui.panels.SongFormatterPanel;

public class App extends JFrame {
    public App() {
        setTitle("Glory Presenter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Songs", new SongFormatterPanel());
        tabs.addTab("Bible", new PPTXGeneratorsPanel());
        tabs.addTab("Settings", new SettingsPanel());
        getContentPane().add(tabs);

        pack(); // auto-resize to component, use setSize if need fixed size
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                AppLogger.showError("Oops!: " + e.getMessage() + "(see error.log for details)", e);
            });

            App app = new App();
            app.setVisible(true);
        });
    }
}
