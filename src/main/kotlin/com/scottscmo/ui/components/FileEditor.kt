package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import net.miginfocom.swing.MigLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class FileEditor(initDir: String, filePickerLabel: String, initContent: String = "") {
    val ui = JPanel()

    private val filePicker = JButton(filePickerLabel)
    private val textArea = JTextArea(initContent)
    private val saveButton = JButton("Save")
    private val reloadButton = JButton("Reload")

    private var filePath = ""

    init {
        ui.apply {
            layout = MigLayout("ins 0")
            add(filePicker, "wrap, span, growx")
            add(JScrollPane(textArea.apply { columns = 30 }), "wrap, span, grow")
            add(reloadButton)
            add(saveButton)
        }

        filePicker.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                FilePicker.show("file", Config.getRelativePath(initDir)) { selectedPath ->
                    filePath = selectedPath
                    loadFileToTextArea(filePath, textArea)
                }
            }
        })

        saveButton.addActionListener {
            Files.writeString(Path.of(filePath), textArea.text)
        }

        reloadButton.addActionListener {
            loadFileToTextArea(filePath, textArea)
        }
    }

    val content: String
        get() = textArea.text

    val path: String
        get() = filePath

    companion object {
        private fun loadFileToTextArea(path: String, textArea: JTextArea) {
            try {
                val content = Files.readString(Path.of(path), StandardCharsets.UTF_8)
                textArea.apply {
                    text = content
                    caretPosition = 0 // scroll to top
                }
            } catch (e: IOException) {
                OutputDisplay.error("Error getting content for song $path")
            }
        }
    }
}