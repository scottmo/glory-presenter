package com.scottmo;

import static com.scottmo.config.Config.UI_GAP;

import java.awt.KeyboardFocusManager;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import com.scottmo.ui.containers.*;
import org.httprpc.sierra.ScrollingKeyboardFocusManager;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.shared.Pair;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.UiConfigurator;

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
    }

    private void render() {
        JTabbedPane tabs = new JTabbedPane();

        // Define tab info: label key -> tab factory
        List<Pair<String, Supplier<JPanel>>> tabFactories = List.of(
            new Pair<>("songs.containerTitle", SongTab::new),
            new Pair<>("bible.containerTitle", BibleTab::new),
            new Pair<>("program.containerTitle", ProgramTab::new),
            new Pair<>("formatter.containerTitle", FormatterTab::new),
            new Pair<>("configs.containerTitle", ConfigsTab::new)
        );

        // Track which tabs have been loaded
        boolean[] loaded = new boolean[tabFactories.size()];

        // Add placeholder panels
        for (var tabFactory : tabFactories) {
            tabs.addTab(Labels.get(tabFactory.key()), new JPanel());
        }

        // Lazy load on tab selection
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            if (index >= 0 && !loaded[index]) {
                JPanel actualTab = tabFactories.get(index).value().get();
                actualTab.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
                tabs.setComponentAt(index, actualTab);
                loaded[index] = true;
            }
        });

        // Load first tab immediately
        JPanel firstTab = tabFactories.get(0).value().get();
        firstTab.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        tabs.setComponentAt(0, firstTab);
        loaded[0] = true;

        getContentPane().add(tabs);

        var appSize = configService.getConfig().getAppSize();
        setSize(appSize.width(), appSize.height());
        setLocationRelativeTo(null); // center on screen
        this.setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.uiScale", ConfigService.get().getConfig().getUiScale());
        FlatCarbonIJTheme.setup();

        UiConfigurator.initializeGlobalFont();

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
