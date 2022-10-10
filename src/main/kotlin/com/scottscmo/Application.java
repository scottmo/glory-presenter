package com.scottscmo;

import com.formdev.flatlaf.FlatLightLaf;
import com.scottscmo.ui.OutputDisplay;
import com.scottscmo.ui.components.DataPathPicker;
import com.scottscmo.ui.panels.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Application extends JFrame {

    public Application() {
        setTitle("Worship Service Tool");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        LayoutManager layout = new MigLayout("ins 0, wrap");
        getContentPane().setLayout(layout);

        JComponent dataPathPicker = new DataPathPicker().getUi();
        getContentPane().add(dataPathPicker);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bible", new BibleInfoPanel());
        tabs.addTab("Song Formatter", new SongFormatterPanel());
        tabs.addTab("PPTX Generators", new PPTXGeneratorsPanel());
        tabs.addTab("Google Slides", new GSlidesPanel());
        tabs.addTab("Settings", new SettingsPanel());
        getContentPane().add(tabs);

        pack(); // auto-resize to component, use setSize if need fixed size
    }

    private static Application _app;
    public static Application get() {
        return _app;
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
                defaultHandler.uncaughtException(thread, e);
                // show uncaught error to user
                OutputDisplay.INSTANCE.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            });

            _app = new Application();
            _app.setVisible(true);
        });
    }
}

