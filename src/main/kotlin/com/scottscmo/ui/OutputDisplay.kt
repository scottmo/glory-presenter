package com.scottscmo.ui

import javax.swing.JFrame
import javax.swing.JOptionPane

object OutputDisplay {
    var app: JFrame? = null

    fun show(msg: String?) {
        JOptionPane.showMessageDialog(app, msg);
    }

    fun error(msg: String?) {
        JOptionPane.showMessageDialog(app, msg, msg, JOptionPane.ERROR_MESSAGE);
    }
}
