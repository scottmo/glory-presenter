package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.Event
import com.scottscmo.ui.FilePicker
import java.nio.file.Path
import javax.swing.JButton

private const val BUTTON_TEXT_PREFIX = "Data Path: "

class DataPathPicker {
    val ui = JButton()

    init {
        ui.apply {
            text = BUTTON_TEXT_PREFIX + Path.of(Config.get().dataDir)
            addActionListener { _ ->
                FilePicker.show("directory") { newDataPath ->
                    text = BUTTON_TEXT_PREFIX + newDataPath
                    Config.get().dataDir = newDataPath
                    Event.emit(Event.DATA_DIR, newDataPath)
                }
            }
        }
    }
}
