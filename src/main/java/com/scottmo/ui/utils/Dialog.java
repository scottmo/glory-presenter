package com.scottmo.ui.utils;

import javax.swing.*;

import org.apache.log4j.Logger;

import com.scottmo.App;

public class Dialog {

    public static boolean showDialogs = true;

    public static void info(String msg) {
        if (!showDialogs || java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("INFO: " + msg);
            return;
        }
        JOptionPane.showMessageDialog(App.get(), msg, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(String msg) {
        if (!showDialogs || java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("WARNING: " + msg);
            return;
        }
        JOptionPane.showMessageDialog(App.get(), msg, "WARNING", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(String msg, Throwable e) {
        if (!showDialogs || java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println("ERROR: " + msg);
            if (e != null) {
                Logger.getLogger(Dialog.class.getName()).error(msg, e);
            }
            return;
        }
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
