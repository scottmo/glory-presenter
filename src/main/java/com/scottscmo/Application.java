package com.scottscmo;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.scottscmo.ui.Labels;
import com.scottscmo.ui.container.CommandRunner;
import com.scottscmo.ui.container.DataPathPicker;
import com.scottscmo.ui.container.SongFormatter;

import net.miginfocom.swing.MigLayout;

public class Application {

    public static void main(String[] args) {
        FlatCarbonIJTheme.setup();
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                render();
            }
        });
    }

    private static void render() {
        JFrame frame = new JFrame(Labels.APP_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        renderContent(frame.getContentPane());

        // auto-resize to component, use setSize if need fixed size
        frame.pack();
        // display window
        frame.setVisible(true);
    }

    private static void renderContent(Container appContainer) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Labels.TAB_SONG_FORMATTER, new SongFormatter());
        tabbedPane.addTab(Labels.TAB_COMMAND_RUNNER, new CommandRunner());

        appContainer.setLayout(new MigLayout());
        appContainer.add(DataPathPicker.create(appContainer), "wrap");
        appContainer.add(tabbedPane);
    }
}
