package com.scottscmo;

import java.io.File;
import java.nio.file.Path;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.scottscmo.ui.Labels;
import com.scottscmo.ui.container.CommandRunner;
import com.scottscmo.ui.container.DataPathPicker;
import com.scottscmo.ui.container.SongFormatter;

import net.miginfocom.swing.MigLayout;

public class Application {

    private static void renderContent(Container appContainer) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Labels.TAB_SONG_FORMATTER, new SongFormatter());
        tabbedPane.addTab(Labels.TAB_COMMAND_RUNNER, new CommandRunner());

        appContainer.setLayout(new MigLayout());
        appContainer.add(DataPathPicker.create(appContainer), "wrap");
        appContainer.add(tabbedPane);
    }

    public static void main(String[] args) {
        FlatCarbonIJTheme.setup();
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame(Labels.APP_NAME);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                renderContent(frame.getContentPane());

                // auto-resize to component, use setSize if need fixed size
                frame.pack();
                // display window
                frame.setVisible(true);
            }
        });
    }
}
