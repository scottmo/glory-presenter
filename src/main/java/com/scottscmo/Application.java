package com.scottscmo;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.scottscmo.ui.Labels;
import com.scottscmo.ui.container.SongFormatter;

public class Application {

    private static void render() {
        JFrame frame = new JFrame(Labels.APP_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Labels.SONG_FORMATTER_TAB, new SongFormatter());
        frame.getContentPane().add(tabbedPane);

        // auto-resize to component, use setSize if need fixed size
        frame.pack();

        // display window
        frame.setVisible(true);
    }

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
}
