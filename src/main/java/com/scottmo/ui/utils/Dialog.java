package com.scottmo.ui.utils;

import javax.swing.*;

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
        JDialog modalDialog = new JDialog(App.get(), title, true); // 'true' makes it modalSwingU
        modalDialog.setSize(App.get().getWidth() - 100, App.get().getHeight() - 100);
        modalDialog.setMinimumSize(content.getMinimumSize());
        modalDialog.add(content);
        return modalDialog;
    }
}
