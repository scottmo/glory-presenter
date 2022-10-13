package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.Event
import com.scottscmo.ui.FilePicker
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JLabel

private const val BUTTON_TEXT_PREFIX = "Data Path: "

class DataPathPicker {
    val ui = JLabel()

    init {
        ui.apply {
            text = BUTTON_TEXT_PREFIX + Path.of(Config.get().dataDir())
        }
    }
}
