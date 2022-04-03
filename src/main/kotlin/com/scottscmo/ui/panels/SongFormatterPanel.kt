package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.model.song.converters.HTMLConverter
import com.scottscmo.model.song.converters.MDConverter
import com.scottscmo.model.song.converters.YAMLConverter
import com.scottscmo.ui.components.FileEditor
import net.miginfocom.swing.MigLayout
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

class SongFormatterPanel : JPanel() {
    private val songEditor = FileEditor(Config.SONG_YAML_DIR, "Select Song")

    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val transformButton = JButton("Transform")
    private val saveTransformedButton = JButton("Save as Slide-Format Song")
    private val saveAsHTMLButton = JButton("Save as HTML")
    private val saveAsMDButton = JButton("Save as MD")
    private val outputTextArea = JTextArea(25, 45).apply {
        font = Config.textAreaFont
    }

    private val songSlideEditor = FileEditor(Config.SONG_SLIDES_DIR, "Select Stored Slide-Format Song")

    init {
        layout = MigLayout("wrap 3", "sg main, grow, left", "top")

        // yaml song picker
        add(songEditor.ui)
        // yaml to slide text/csv transformer
        add(JPanel().apply {
            layout = MigLayout("wrap, ins 0")
            add(JPanel().apply {
                layout = MigLayout("ins 0")
                add(transformButton)
                add(maxLinesSpinnerInput)
                add(JLabel("Lines/Section/Text Group"))
            }, "span")
            add(JScrollPane(outputTextArea), "span, grow")
            add(saveTransformedButton)
            add(saveAsHTMLButton)
            add(saveAsMDButton)
        })
        // slide text
        add(songSlideEditor.ui)

        transformButton.addActionListener {
            handleTransformSong(songEditor.content, getMaxLines(), outputTextArea)
        }

        saveTransformedButton.addActionListener {
            handleSaveTransformed(outputTextArea.text)
        }

        saveAsHTMLButton.addActionListener {
            handleSaveAsHTML(outputTextArea.text, getMaxLines())
        }

        saveAsMDButton.addActionListener {
            handleSaveAsMD(outputTextArea.text, getMaxLines())
        }
    }

    private fun getMaxLines(): Int {
        return maxLinesSpinnerInput.value as Int
    }

    companion object {
        private fun handleTransformSong(serializedSong: String, maxLines: Int, outputTextArea: JTextArea) {
            YAMLConverter.parse(serializedSong)?.let { song ->
                outputTextArea.apply {
                    text = YAMLConverter.stringify(song, Config.get().googleSlideConfig.textConfigsOrder, maxLines)
                    caretPosition = 0 // scroll to top
                }
            }
        }

        private fun handleSaveTransformed(serializedSong: String) {
            val song = YAMLConverter.parse(serializedSong)
            requireNotNull(song) { "Unable to convert song!" }

            val filePath = Config.getRelativePath("${Config.SONG_SLIDES_DIR}/${song.title}.yaml")
            Files.writeString(Path.of(filePath), serializedSong)
        }

        private fun handleSaveAsHTML(serializedSong: String, maxLines: Int) {
            val song = YAMLConverter.parse(serializedSong)
            requireNotNull(song) { "Unable to convert song!" }

            val filePath = Config.getOutputDir("${song.title}.html")
            val content = HTMLConverter.stringify(song, Config.get().googleSlideConfig.textConfigsOrder, maxLines)
            Files.writeString(Path.of(filePath), content)
        }

        private fun handleSaveAsMD(serializedSong: String, maxLines: Int) {
            val song = YAMLConverter.parse(serializedSong)
            requireNotNull(song) { "Unable to convert song!" }

            val filePath = Config.getOutputDir("${song.title}.md")
            val content = MDConverter.stringify(song, Config.get().googleSlideConfig.textConfigsOrder, maxLines)
            Files.writeString(Path.of(filePath), content)
        }
    }
}
