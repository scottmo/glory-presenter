package com.scottmo;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public final class AppLogger {
    private static final Logger logger = Logger.getLogger(AppLogger.class.getName());

    public static void show(String msg) {
        JOptionPane.showMessageDialog(Application.get(), msg);
    }

    public static void showError(String msg, Throwable e) {
        JOptionPane.showMessageDialog(Application.get(), msg, msg, JOptionPane.ERROR_MESSAGE);
        error(msg, e);
    }

    public static void error(String msg, Throwable e) {
        logger.error(msg, e);
        e.printStackTrace();
    }
}
