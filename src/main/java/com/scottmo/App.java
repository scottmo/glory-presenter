package com.scottmo;

import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.config.ConfigService;
import com.scottmo.core.logging.api.LoggingService;
import com.scottmo.ui.panels.PPTXGeneratorsPanel;
import com.scottmo.ui.panels.SettingsPanel;
import com.scottmo.ui.panels.SongFormatterPanel;

public class App extends JFrame {
    ConfigService configService = ConfigService.get();
    Supplier<LoggingService> logger = ServiceProvider.get(LoggingService.class);

    public App() {
        setTitle(configService.getLabel("title"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(configService.getLabel("tabs.songs"), new SongFormatterPanel());
        tabs.addTab(configService.getLabel("tabs.bible"), new PPTXGeneratorsPanel());
        tabs.addTab(configService.getLabel("tabs.configs"), new SettingsPanel());
        getContentPane().add(tabs);

        pack(); // auto-resize to component, use setSize if need fixed size
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                ServiceProvider.get(LoggingService.class).get()
                    .error(String.format("[Error] %s (see error.log for details)", e.getMessage()), e);
            });

            App app = new App();
            app.setVisible(true);
        });
    }
}
