package com.scottscmo.ui.container

import com.scottscmo.Config
import com.scottscmo.Config.DATA_DIR
import com.scottscmo.model.song.adapters.SongCSVAdapter
import com.scottscmo.model.song.adapters.SongSlideTextAdapter
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.OutputDisplay
import com.scottscmo.ui.components.C
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.nio.file.Path
import javax.swing.*

class SongFormatter : JPanel() {
    private val songList = JList(emptyArray<String>())
    private val songTextArea = JTextArea()
    private val outputTextArea = JTextArea()
    private val transformButton = JButton("Transform")
    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val songSearchInput = JTextField()
    private val saveAsCSVButton = JButton("Save as CSV")

    private var songTitles: List<String> = emptyList()

    init {
        minimumSize = Dimension(640, 480)
        layout = BorderLayout(10, 10)

        add(C.resizableHBox(
            JPanel(MigLayout("wrap 5")).apply {
                add(JLabel("Search Song"))
                add(songSearchInput.apply {
                    columns = 20
                }, "span, align left")
                add(songList.apply {
                    fixedCellHeight = 16
                    fixedCellWidth = 400
                    visibleRowCount = 10
                    selectionMode = ListSelectionModel.SINGLE_SELECTION
                }, "span")
            },
            songTextArea.apply { columns = 30 },
            outputTextArea.apply { columns = 30 }
        ), BorderLayout.CENTER)

        add(JPanel().apply {
            add(JLabel("Lines Per Slide Per Language"))
            add(maxLinesSpinnerInput)
            add(transformButton)
            add(saveAsCSVButton)
        }, BorderLayout.SOUTH)

        // controllers
        Config.subscribe(DATA_DIR, true) { dataPath ->
            handleLoadSongList(dataPath)
        }

        songList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                handleLoadSong()
            }
        })

        transformButton.addActionListener {
            handleTransformSong()
        }

        songSearchInput.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ENTER) return

                handleSearchSong()
            }
        })

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

    private fun handleLoadSongList(dataPath: String) {
        songTitles = try {
            File(Path.of(dataPath, "songs").toString()).listFiles()
                ?.map { f -> f.name }
                ?.filter { fname -> fname.endsWith(".yaml") }
                ?.map { fname -> fname.replace(".yaml", "") }
                ?.sorted()
            ?: emptyList()
        } catch (e: IOException) {
            System.err.println(e.message)
            listOf("Unable to load songs!")
        }
        songList.setListData(songTitles.toTypedArray())
    }

    private fun handleLoadSong() {
        val songName = songList.selectedValue as String
        songTextArea.apply {
            text = SongYAMLAdapter.getSerializedSong(songName)
                ?: "Error getting content for song $songName"
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

    private fun handleSearchSong() {
        songList.setListData(songTitles
            .filter { title -> title.contains(songSearchInput.text) }
            .toTypedArray())
    }
}
