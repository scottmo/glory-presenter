package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.Config.DIR_DATA
import javax.swing.JComponent
import javax.swing.JButton
import java.awt.Component
import java.nio.file.Path
import javax.swing.JFileChooser

object DataPathPicker {
    private const val BUTTON_TEXT_PREFIX = "Data Path: "
    fun create(host: Component): JComponent {
        val dataPath = Path.of(Config[DIR_DATA])
        val setDataPathBtn = JButton(BUTTON_TEXT_PREFIX + dataPath.toString())

        setDataPathBtn.addActionListener { _ ->
            val fc = JFileChooser()
            fc.currentDirectory = dataPath.toFile() // start at application current directory
            fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = fc.showSaveDialog(host)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val newDataPath = fc.selectedFile.toPath().toAbsolutePath().toString()
                setDataPathBtn.text = BUTTON_TEXT_PREFIX + newDataPath
                Config[DIR_DATA] = newDataPath
            }
        }
        return setDataPathBtn
    }
}
