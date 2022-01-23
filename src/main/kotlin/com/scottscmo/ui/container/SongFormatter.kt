package com.scottscmo.ui.container

import com.scottscmo.Config
import com.scottscmo.Config.DIR_DATA
import com.scottscmo.song.adapters.SongSlideTextAdapter
import com.scottscmo.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.components.C
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.*

class SongFormatter : JPanel() {
    private val songList = JList(emptyArray<String>())
    private val songTextArea = JTextArea()
    private val outputTextArea = JTextArea()
    private val transformButton = JButton("Transform")
    private val maxLinesSpinnerInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
    private val songSearchInput = JTextField()

    init {
        this.layout = BorderLayout(10, 10)
        this.add(
            C.resizableHBox(
                JPanel(MigLayout("wrap 5")).apply {
                    this.add(JLabel("Search Song"))
                    this.add(songSearchInput
                        .apply {
                            this.columns = 20
                        }, "span, align left")
                    this.add(songList
                        .apply {
                            this.fixedCellHeight = 16
                            this.fixedCellWidth = 400
                            this.visibleRowCount = 10
                            this.selectionMode = ListSelectionModel.SINGLE_SELECTION
                        }, "span")
                },
                songTextArea
                    .apply {
                        this.columns = 30
                    },
                outputTextArea
                    .apply {
                        this.columns = 20
                    }

            ),
            BorderLayout.CENTER
        )
        this.add(
            JPanel().apply {
                this.add(JLabel("Lines Per Slide Per Language"))
                this.add(maxLinesSpinnerInput)
                this.add(transformButton)
            },
            BorderLayout.SOUTH
        )

        // controllers
        Config.subscribe(DIR_DATA, true) { dataPath ->
            var newSongTitles = getSongTitles(true)
            if (newSongTitles.isEmpty()) {
                newSongTitles = listOf("Unable to load songs!")
            }
            songList.setListData(newSongTitles.toTypedArray())
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
    }

    private var songTitles: List<String> = emptyList()
    private fun getSongTitles(bustCache: Boolean = false): List<String> {
        val dirSongPath = Path.of(Config[DIR_DATA], "songs")
        if (bustCache || songTitles.isEmpty()) {
            songTitles = try {
                Files.list(dirSongPath)
                        .map { path -> path.fileName.toString() }
                        .filter { path -> path.endsWith(".yaml") }
                        .map { path -> path.replace(".yaml", "") }
                        .sorted()
                        .toList()
            } catch (e: IOException) {
                System.err.println(e.message)
                emptyList()
            }
        }
        return songTitles
    }

    private fun handleLoadSong() {
        val songName = songList.selectedValue as String
        songTextArea.text = SongYAMLAdapter.getSerializedSong(songName)
            ?: "Error getting content for song $songName"
    }

    private fun handleTransformSong() {
        val song = SongYAMLAdapter.deserialize(songTextArea.text)
        outputTextArea.text = SongSlideTextAdapter.serialize(song, listOf("zh", "en"), maxLinesSpinnerInput.value as Int)
    }

    private fun handleSearchSong() {
        songList.setListData(getSongTitles()
            .filter { title -> title.contains(songSearchInput.text) }
            .toTypedArray())
    }
}
