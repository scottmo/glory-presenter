package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.model.song.converters.KVMDConverter
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

        saveAsMDButton.addActionListener {
            handleSaveAsMD(outputTextArea.text, getMaxLines())
        }
    }

    private fun getMaxLines(): Int {
        return maxLinesSpinnerInput.value as Int
    }

    companion object {
        private val SINGLE_LINE_VERSE = Regex("(\\s{4})(\\w+):\\s([^|][^-].+)")
        private val MULTI_LINE_VERSE_REPL = "$1$2: |-\n$1  $3"

        private fun handleTransformSong(serializedSong: String, maxLines: Int, outputTextArea: JTextArea) {
            YAMLConverter.parse(serializedSong)?.let { song ->
                var transformedText = YAMLConverter.stringify(song, Config.get().googleSlideConfig.textConfigsOrder, maxLines)
                transformedText = transformedText.replace(SINGLE_LINE_VERSE, MULTI_LINE_VERSE_REPL)

                outputTextArea.apply {
                    text = transformedText
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

        private fun handleSaveAsMD(serializedSong: String, maxLines: Int) {
            val song = YAMLConverter.parse(serializedSong)
            requireNotNull(song) { "Unable to convert song!" }

            val filePath = Config.getOutputDir("${song.title}.md")
            val content = KVMDConverter.stringify(song, Config.get().googleSlideConfig.textConfigsOrder, maxLines)
            Files.writeString(Path.of(filePath), content)
        }
    }
}
