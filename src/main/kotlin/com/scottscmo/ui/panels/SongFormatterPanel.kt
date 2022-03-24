package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.model.song.adapters.SongCSVAdapter
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.FileEditor
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

// using this to expand the textarea to max height for visual purpose
private val EMPTY_TEXT_PLACEHOLDER = "\n".repeat(30)

class SongFormatterPanel : JPanel() {
    private val songEditor = FileEditor(Config.SONG_YAML_DIR, "Select Song", EMPTY_TEXT_PLACEHOLDER)

    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val transformButton = JButton("Transform")
    private val saveAsCSVButton = JButton("Save as CSV")
    private val saveAsTXTButton = JButton("Save as Slide-Format Song")
    private val outputTextArea = JTextArea(EMPTY_TEXT_PLACEHOLDER)

    private val songSlideEditor = FileEditor(Config.SONG_SLIDES_DIR, "Select Stored Slide-Format Song", EMPTY_TEXT_PLACEHOLDER)

    init {
        preferredSize = Dimension(640, 480)
        layout = MigLayout("wrap 3", "sg main, grow, left", "top")

        // yaml song picker
        add(songEditor.ui)
        // yaml to slide text/csv transformer
        add(JPanel().apply {
            layout = MigLayout("ins 0")
            add(JPanel().apply {
                add(JLabel("Lines Per Slide Per Language"))
                add(maxLinesSpinnerInput)
                add(transformButton)
            }, "wrap, span")
            add(JScrollPane(outputTextArea.apply { columns = 30 }), "wrap, span, grow")
            add(saveAsCSVButton)
            add(saveAsTXTButton)
        })
        // slide text
        add(songSlideEditor.ui)

        transformButton.addActionListener {
            handleTransformSong(songEditor.content, getMaxLines(), outputTextArea)
        }

        saveAsCSVButton.addActionListener {
            handleSaveAsCSV(songEditor.content, getMaxLines())
        }

        saveAsTXTButton.addActionListener {
            handleSaveAsTxt(outputTextArea.text)
        }
    }

    private fun getMaxLines(): Int {
        return maxLinesSpinnerInput.value as Int
    }

    companion object {
        private fun handleSaveAsCSV(serializedSong: String, maxLines: Int) {
            val song = SongYAMLAdapter.deserialize(serializedSong)
            if (song != null) {
                try {
                    val filePath = Config.getRelativePath("${Config.SONG_CSV_DIR}/${song.title}.csv")
                    SongCSVAdapter.serializeToCSV(filePath, song, listOf("zh", "en"), maxLines)
                    OutputDisplay.show("Saved successfully!")
                } catch (e: IOException) {
                    OutputDisplay.error("Unable to save to CSV: ${e.message}")
                }
            } else {
                OutputDisplay.error("Unable to convert song!")
            }
        }

        private fun handleTransformSong(serializedSong: String, maxLines: Int, outputTextArea: JTextArea) {
            SongYAMLAdapter.deserialize(serializedSong)?.let { song ->
                outputTextArea.apply {
                    text = SongYAMLAdapter.serialize(song, listOf("zh", "en"), maxLines)
                    caretPosition = 0 // scroll to top
                }
            }
        }

        private fun handleSaveAsTxt(serializedSong: String) {
            val song = SongYAMLAdapter.deserialize(serializedSong)
            if (song != null) {
                val filePath = Config.getRelativePath("${Config.SONG_SLIDES_DIR}/${song.title}.yaml")
                Files.writeString(Path.of(filePath), serializedSong)
            } else {
                OutputDisplay.error("Unable to convert song!")
            }
        }
    }
}
