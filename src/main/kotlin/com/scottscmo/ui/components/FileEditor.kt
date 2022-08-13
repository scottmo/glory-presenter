package com.scottscmo.ui.components

import com.scottscmo.Config
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import net.miginfocom.swing.MigLayout
import java.awt.Font
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

private const val DEFAULT_HEIGHT = 25
private const val DEFAULT_WIDTH = 45

fun FileEditor(pathString: String, filePickerLabel: String = pathString,
        editorHeight: Int = DEFAULT_HEIGHT, editorWidth: Int = DEFAULT_WIDTH) =
    FileEditor(Path.of(Config.getRelativePath(pathString)), filePickerLabel, editorHeight, editorWidth)

class FileEditor(path: Path, filePickerLabel: String = path.toString(),
        editorHeight: Int = DEFAULT_HEIGHT, editorWidth: Int = DEFAULT_WIDTH) {
    val ui = JPanel()
    var filePath = ""

    private val filePicker = JButton(filePickerLabel)
    private val textArea = JTextArea(editorHeight, editorWidth).apply {
        font = Config.textAreaFont
    }
    private val saveButton = JButton("Save")
    private val reloadButton = JButton("Reload")

    init {
        ui.apply {
            layout = MigLayout("ins 0")
            add(filePicker, "wrap, span, growx")
            add(JScrollPane(textArea), "wrap, span, grow")
            add(reloadButton)
            add(saveButton)
        }

        val isFilePickerEnabled = Files.isDirectory(path)
        if (isFilePickerEnabled) {
            filePicker.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(me: MouseEvent) {
                    FilePicker.show("file", path.toString()) { selectedPath ->
                        filePath = selectedPath
                        loadFileToTextArea(filePath, textArea)
                        toggleReadWriteButtons(true)
                    }
                }
            })
        } else {
            filePath = path.toString()
            filePicker.text = "Load $filePath"
            filePicker.addActionListener {
                filePicker.isEnabled = false
                filePicker.text = filePath
                loadFileToTextArea(filePath, textArea)
                toggleReadWriteButtons(true)
            }
        }

        saveButton.addActionListener {
            Files.writeString(Path.of(filePath), textArea.text)
        }

        reloadButton.addActionListener {
            loadFileToTextArea(filePath, textArea)
        }

        toggleReadWriteButtons(false)
    }

    val content: String
        get() = textArea.text

    val path: String
        get() = filePath

    fun toggleReadWriteButtons(isEnabled: Boolean) {
        saveButton.isEnabled = isEnabled
        reloadButton.isEnabled = isEnabled
    }

    companion object {
        private fun loadFileToTextArea(path: String, textArea: JTextArea) {
            try {
                val content = Files.readString(Path.of(path), StandardCharsets.UTF_8)
                textArea.apply {
                    text = content
                    caretPosition = 0 // scroll to top
                }
            } catch (e: IOException) {
                OutputDisplay.error("Unable to load $path")
            }
        }
    }
}