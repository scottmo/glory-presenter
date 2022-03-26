package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.FileEditor
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

class SongFormatterPanel : JPanel() {
    private val songEditor = FileEditor(Config.SONG_YAML_DIR, "Select Song")

    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val transformButton = JButton("Transform")
    private val saveAsTXTButton = JButton("Save as Slide-Format Song")
    private val outputTextArea = JTextArea(25, 30)

    private val songSlideEditor = FileEditor(Config.SONG_SLIDES_DIR, "Select Stored Slide-Format Song")

    init {
        layout = MigLayout("wrap 3", "sg main, grow, left", "top")

        // yaml song picker
        add(songEditor.ui)
        // yaml to slide text/csv transformer
        add(JPanel().apply {
            layout = MigLayout("wrap, ins 0")
            add(JScrollPane(outputTextArea), "span, grow")
            add(JPanel().apply {
                layout = MigLayout("ins 0")
                add(transformButton)
                add(maxLinesSpinnerInput)
                add(JLabel("Lines Per Slide Per Language"))
            }, "span")
            add(saveAsTXTButton)
        })
        // slide text
        add(songSlideEditor.ui)

        transformButton.addActionListener {
            handleTransformSong(songEditor.content, getMaxLines(), outputTextArea)
        }

        saveAsTXTButton.addActionListener {
            handleSaveAsTxt(outputTextArea.text)
        }
    }

    private fun getMaxLines(): Int {
        return maxLinesSpinnerInput.value as Int
    }

    companion object {
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
