package com.scottscmo.ui

import com.scottscmo.Config
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.JFrame

object FilePicker {
    var host: JFrame? = null

    fun show(mode: String = "fileAndDirectory", defaultPath: String = Config.get().dataDir,
            onSelected: (filePath: String) -> Unit) {

        val fc = JFileChooser().apply {
            currentDirectory = Path.of(defaultPath).toFile() // start at application data directory
            fileSelectionMode = when (mode) {
                "file" -> JFileChooser.FILES_ONLY
                "directory" -> JFileChooser.DIRECTORIES_ONLY
                else -> JFileChooser.FILES_AND_DIRECTORIES
            }
        }
        val returnVal = fc.showSaveDialog(host)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val selectedPath = fc.selectedFile.toPath().toAbsolutePath().toString()
            onSelected(selectedPath)
        }
    }
}