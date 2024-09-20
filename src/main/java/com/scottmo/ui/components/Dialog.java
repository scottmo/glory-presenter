package com.scottmo.ui.components;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.scottmo.App;

public class Dialog {

    public static void info(String msg) {
        JOptionPane.showMessageDialog(App.get(), msg, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(String msg) {
        JOptionPane.showMessageDialog(App.get(), msg, "WARNING", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(String msg, Throwable e) {
        JOptionPane.showMessageDialog(App.get(), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        if (e != null) {
            Logger.getLogger(Dialog.class.getName()).error(msg, e);
        }
    }
    public static void error(String msg) {
        error(msg, null);
    }

    public static JDialog newModal(String title, JPanel content) {
        JDialog modalDialog = new JDialog(App.get(), title, true); // 'true' makes it modal
        // modalDialog.setSize(APP_WIDTH - 100, APP_HEIGHT - 100);
        modalDialog.setLocationRelativeTo(App.get()); // Center the dialog relative to the main frame

        modalDialog.setMinimumSize(content.getMinimumSize());
        modalDialog.add(content);
        modalDialog.pack();

        return modalDialog;
    }
}
