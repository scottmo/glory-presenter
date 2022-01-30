package com.scottscmo.ui.container

import com.scottscmo.Config
import com.scottscmo.model.song.adapters.SongCSVAdapter
import com.scottscmo.model.song.adapters.SongSlideTextAdapter
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.FilePicker
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.C
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

class SongFormatter : JPanel() {
    private val songPicker = JButton("Select Song YAML")
    private val songTextArea = JTextArea()
    private val outputTextArea = JTextArea()
    private val transformButton = JButton("Transform")
    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val saveAsCSVButton = JButton("Save as CSV")

    init {
        minimumSize = Dimension(640, 480)
        layout = BorderLayout(10, 10)

        add(songPicker, BorderLayout.NORTH)
        add(C.resizableHBox(
            songTextArea.apply { columns = 30 },
            outputTextArea.apply { columns = 30 }
        ), BorderLayout.CENTER)

        add(JPanel().apply {
            add(JLabel("Lines Per Slide Per Language"))
            add(maxLinesSpinnerInput)
            add(transformButton)
            add(saveAsCSVButton)
        }, BorderLayout.SOUTH)

        songPicker.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                FilePicker.show("file", Config.getRelativePath("songs")) { selectedPath ->
                    handleLoadSongFromPath(selectedPath)
                }
            }
        })

        transformButton.addActionListener {
            handleTransformSong()
        }

        saveAsCSVButton.addActionListener {
            handleSaveAsCSV()
        }
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

    private fun handleLoadSongFromPath(songPath: String) {
        val songContent = try {
            Files.readString(Path.of(songPath), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            "Error getting content for song $songPath"
        }
        songTextArea.apply {
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
