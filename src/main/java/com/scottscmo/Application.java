package com.scottscmo;

import com.formdev.flatlaf.FlatLightLaf;
import com.scottscmo.ui.panels.BibleInfoPanel;
import com.scottscmo.ui.panels.GSlidesPanel;
import com.scottscmo.ui.panels.PPTXGeneratorsPanel;
import com.scottscmo.ui.panels.SettingsPanel;
import com.scottscmo.ui.panels.SongFormatterPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.LayoutManager;

public class Application extends JFrame {

    private static Application _app;
    public static Application get() {
        return _app;
    }

    public Application() {
        setTitle("Worship Service Tool");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        LayoutManager layout = new MigLayout("ins 0, wrap");
        getContentPane().setLayout(layout);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bible", new BibleInfoPanel());
        tabs.addTab("Song Formatter", new SongFormatterPanel());
        tabs.addTab("PPTX Generators", new PPTXGeneratorsPanel());
        tabs.addTab("Google Slides", new GSlidesPanel());
        tabs.addTab("Settings", new SettingsPanel());
        getContentPane().add(tabs);

        pack(); // auto-resize to component, use setSize if need fixed size
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            AppLogger.error("Uncaught exception in thread: " + t.getName(), e);
        });

        Config.reload();
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            _app = new Application();
            _app.setVisible(true);
        });
    }
}

