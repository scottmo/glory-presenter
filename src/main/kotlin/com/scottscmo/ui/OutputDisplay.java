package com.scottscmo.ui;

import com.scottscmo.Application;
import javax.swing.JOptionPane;

public final class OutputDisplay {

    public static void show(String msg) {
        JOptionPane.showMessageDialog(Application.get(), msg);
    }

    public static void error(String msg, Exception e) {
        JOptionPane.showMessageDialog(Application.get(), msg, msg, JOptionPane.ERROR_MESSAGE);
        Application.getLogger().error(msg, e);
    }
}
