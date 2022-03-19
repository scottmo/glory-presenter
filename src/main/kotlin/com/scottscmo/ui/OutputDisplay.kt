package com.scottscmo.ui

import java.lang.Exception
import javax.swing.JFrame
import javax.swing.JOptionPane

object OutputDisplay {
    var host: JFrame? = null

    fun show(msg: String?) {
        JOptionPane.showMessageDialog(host, msg);
    }

    fun error(msg: String?) {
        JOptionPane.showMessageDialog(host, msg, msg, JOptionPane.ERROR_MESSAGE);
    }

    fun error(e: Exception) {
        error(e.message + "\n" + e.stackTrace.copyOfRange(0, 10).joinToString("\n"))
    }
}
