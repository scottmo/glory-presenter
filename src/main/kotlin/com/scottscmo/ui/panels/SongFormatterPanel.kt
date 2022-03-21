package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.model.song.adapters.SongCSVAdapter
import com.scottscmo.model.song.adapters.SongSlideTextAdapter
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

// using this to expand the textarea to max height for visual purpose
private val EMPTY_TEXT_PLACEHOLDER = "\n".repeat(30)

class SongFormatterPanel : JPanel() {
    private val songPicker = JButton("Select Song YAML")
    private val songTextArea = JTextArea(EMPTY_TEXT_PLACEHOLDER)

    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val transformButton = JButton("Transform")
    private val saveAsCSVButton = JButton("Save as CSV")
    private val outputTextArea = JTextArea(EMPTY_TEXT_PLACEHOLDER)

    private val songSlideTextPicker = JButton("Select Stored Song Slide Text")
    private val songSlideTextArea = JTextArea(EMPTY_TEXT_PLACEHOLDER)

    init {
        preferredSize = Dimension(640, 480)
        layout = MigLayout("wrap 3", "sg main, grow, left", "top")

        add(JPanel().apply {
            layout = MigLayout()
            add(songPicker, "wrap, growx")
            add(JScrollPane(songTextArea.apply { columns = 30 }), "grow")
        })
        add(JPanel().apply {
            layout = MigLayout()
            add(JPanel().apply {
                add(JLabel("Lines Per Slide Per Language"))
                add(maxLinesSpinnerInput)
                add(transformButton)
            }, "wrap")
            add(saveAsCSVButton, "wrap")
            add(JScrollPane(outputTextArea.apply { columns = 30 }), "grow")
        })
        add(JPanel().apply {
            layout = MigLayout()
            add(songSlideTextPicker, "wrap, growx")
            add(JScrollPane(songSlideTextArea.apply { columns = 30 }), "grow")
        })

        songPicker.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                FilePicker.show("file", Config.getRelativePath("songs")) { selectedPath ->
                    handleLoadSongFromPath(selectedPath, songTextArea)
                }
            }
        })

        transformButton.addActionListener {
            handleTransformSong()
        }

        saveAsCSVButton.addActionListener {
            handleSaveAsCSV()
        }

        songSlideTextPicker.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                FilePicker.show("file", Config.getRelativePath("songs_txt")) { selectedPath ->
                    handleLoadSongFromPath(selectedPath, songSlideTextArea)
                }
            }
        })
    }

    private fun handleSaveAsCSV() {
        val song = SongYAMLAdapter.deserialize(songTextArea.text)
        if (song != null) {
            try {
                val filePath = Config.getRelativePath("songs_csv/${song.title}.csv")
                SongCSVAdapter.serializeToCSV(filePath, song, listOf("zh", "en"), maxLinesSpinnerInput.value as Int)
                OutputDisplay.show("Saved successfully!")
            } catch (e: IOException) {
                OutputDisplay.error("Unable to save to CSV: ${e.message}")
            }
        } else {
            OutputDisplay.error("Unable to convert song!")
        }
    }

    private fun handleLoadSongFromPath(songPath: String, displayArea: JTextArea) {
        val songContent = try {
            Files.readString(Path.of(songPath), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            "Error getting content for song $songPath"
        }
        displayArea.apply {
            text = songContent
            caretPosition = 0 // scroll to top
        }
    }

    private fun handleTransformSong() {
        val song = SongYAMLAdapter.deserialize(songTextArea.text)
        outputTextArea.apply {
            text = SongSlideTextAdapter.serialize(song, listOf("zh", "en"), maxLinesSpinnerInput.value as Int)
            caretPosition = 0 // scroll to top
        }
    }
}
