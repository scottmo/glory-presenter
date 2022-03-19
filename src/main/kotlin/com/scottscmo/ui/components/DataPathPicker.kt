package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.Event
import com.scottscmo.ui.FilePicker
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JComponent

object DataPathPicker {
    private const val BUTTON_TEXT_PREFIX = "Data Path: "
    fun create(): JComponent {
        val dataPath = Path.of(Config.get().dataDir)
        val setDataPathBtn = JButton(BUTTON_TEXT_PREFIX + dataPath.toString())

        setDataPathBtn.addActionListener { _ ->
            FilePicker.show("directory") { newDataPath ->
                setDataPathBtn.text = BUTTON_TEXT_PREFIX + newDataPath
                Config.get().dataDir = newDataPath
                Event.emit(Event.DATA_DIR, newDataPath)
            }
        }
        return setDataPathBtn
    }
}
