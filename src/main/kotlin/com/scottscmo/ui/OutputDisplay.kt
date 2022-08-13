package com.scottscmo.ui

import com.scottscmo.Application
import java.lang.Exception
import javax.swing.JOptionPane

object OutputDisplay {

    fun show(msg: String?) {
        JOptionPane.showMessageDialog(Application.get(), msg)
    }

    fun error(msg: String?) {
        JOptionPane.showMessageDialog(Application.get(), msg, msg, JOptionPane.ERROR_MESSAGE)
    }

    fun error(e: Exception) {
        error(e.message + "\n" + e.stackTrace.copyOfRange(0, 10).joinToString("\n"))
    }
}
