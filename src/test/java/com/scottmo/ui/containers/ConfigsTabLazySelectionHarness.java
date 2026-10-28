package com.scottmo.ui.containers;

import java.lang.reflect.Field;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.scottmo.App;
import com.scottmo.config.ConfigService;
import com.scottmo.core.operations.ConfigOperations;
import com.scottmo.ui.utils.UiConfigurator;

/** Reproduces lazy Configuration-tab construction while the JTabbedPane is dispatching a selection change. */
public final class ConfigsTabLazySelectionHarness {
    private ConfigsTabLazySelectionHarness() {}

    public static void main(String[] args) {
        AtomicReference<Throwable> uncaught = new AtomicReference<>();
        try {
            Thread.setDefaultUncaughtExceptionHandler((thread, error) -> uncaught.compareAndSet(null, error));
            ConfigService.get().getConfig();
            UiConfigurator.initializeTheme(true);
            if (!(UIManager.getLookAndFeel() instanceof FlatCarbonIJTheme)) {
                throw new AssertionError("Expected FlatLaf before lazy tab selection");
            }

            AtomicReference<JTabbedPane> paneReference = new AtomicReference<>();
            AtomicBoolean loaded = new AtomicBoolean();
            AtomicBoolean reloadOnNextConfigurationSelection = new AtomicBoolean();
            SwingUtilities.invokeAndWait(() -> {
                App testApp = new App();
                installAppSingleton(testApp);
                JTabbedPane tabs = new JTabbedPane();
                tabs.addTab("Songs", new JPanel());
                tabs.addTab("Configurations", new JPanel());
                tabs.setSelectedIndex(0);
                testApp.setContentPane(tabs);
                testApp.setSize(640, 420);
                testApp.setLocation(40, 40);
                tabs.setSize(640, 420);
                tabs.doLayout();
                paneReference.set(tabs);
                tabs.addChangeListener(event -> {
                    int index = tabs.getSelectedIndex();
                    if (index == 1 && loaded.compareAndSet(false, true)) {
                        tabs.setComponentAt(index, new ConfigsTab());
                    } else if (index == 1 && reloadOnNextConfigurationSelection.compareAndSet(true, false)) {
                        ConfigOperations.forApplication().reload();
                        if (!(UIManager.getLookAndFeel() instanceof FlatCarbonIJTheme)) {
                            throw new AssertionError("Explicit config reload applied inside the selection event");
                        }
                    }
                });
            });

            Point tabPoint = new Point();
            clickTab(paneReference.get(), 1, tabPoint);
            SwingUtilities.invokeAndWait(() -> {});
            if (uncaught.get() != null) {
                throw new AssertionError("Tab mouse selection raised an uncaught UI exception", uncaught.get());
            }
            if (!loaded.get() || paneReference.get().getSelectedIndex() != 1
                || !(UIManager.getLookAndFeel() instanceof FlatCarbonIJTheme)) {
                throw new AssertionError("Opening Configurations should not implicitly reload or apply a theme");
            }

            reloadOnNextConfigurationSelection.set(true);
            clickTab(paneReference.get(), 0, tabPoint);
            clickTab(paneReference.get(), 1, tabPoint);
            SwingUtilities.invokeAndWait(() -> {});
            if (uncaught.get() != null) {
                throw new AssertionError("Explicit config reload raised an uncaught UI exception", uncaught.get());
            }
            if (paneReference.get().getSelectedIndex() != 1
                || !(UIManager.getLookAndFeel() instanceof FlatCyanLightIJTheme)) {
                throw new AssertionError("Explicit config reload did not apply its deferred theme change");
            }
            System.out.println("CONFIG_TAB_LAZY_SELECTION_OK");
        } catch (Throwable error) {
            error.printStackTrace(System.err);
            System.exit(1);
        } finally {
            App app = App.get();
            if (app != null) {
                App testApp = app;
                try {
                    SwingUtilities.invokeAndWait(testApp::dispose);
                } catch (Exception ignored) {
                    // Process cleanup below is the final fallback if AWT failed during the regression.
                }
            }
        }
        System.exit(0);
    }

    private static void clickTab(JTabbedPane tabs, int index, Point point) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            point.setLocation(tabs.getBoundsAt(index).getCenterX(), tabs.getBoundsAt(index).getCenterY());
            long when = System.currentTimeMillis();
            tabs.dispatchEvent(new MouseEvent(tabs, MouseEvent.MOUSE_PRESSED, when,
                MouseEvent.BUTTON1_DOWN_MASK, point.x, point.y, 1, false, MouseEvent.BUTTON1));
            tabs.dispatchEvent(new MouseEvent(tabs, MouseEvent.MOUSE_RELEASED, when + 1,
                0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
        });
    }

    private static void installAppSingleton(App app) {
        try {
            Field instance = App.class.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, app);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to install test application", e);
        }
    }
}
