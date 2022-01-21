package com.scottscmo.ui.container

import com.scottscmo.Config
import javax.swing.JPanel
import javax.swing.JList
import javax.swing.JTextArea
import javax.swing.JLabel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.JButton
import javax.swing.JTextField
import java.util.stream.Collectors
import java.util.function.Supplier
import java.awt.BorderLayout
import com.scottscmo.ui.components.C
import javax.swing.ListSelectionModel
import java.io.IOException
import com.scottscmo.song.adapters.SongYAMLAdapter
import com.scottscmo.song.adapters.SongSlideTextAdapter
import net.miginfocom.swing.MigLayout
import java.awt.event.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class SongFormatter : JPanel() {
    init {
        // components
        val songList = songList
        val songViewer = songViewer
        val outputViewer = outputViewer
        val maxLinesLabel = JLabel(MAX_LINES_INPUT_LABEL)
        val maxLinesInput = JSpinner(SpinnerNumberModel(1, 1, 10, 1))
        val transformButton = JButton(TRANSFORM_BUTTON)
        val songSearchInputLabel = JLabel(SONG_SEARCH_INPUT_LABEL)
        val songSearchInput = JTextField()

        // behavior
        songList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                loadSong(songList, songViewer)
            }
        })
        transformButton.addActionListener { e: ActionEvent? -> transformSong(songViewer, outputViewer, maxLinesInput.value as Int) }
        songSearchInput.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    var songTitles = getSongTitles()!!.stream()
                            .filter { title: String -> title.contains(songSearchInput.text) }
                            .collect(Collectors.toCollection(Supplier<Vector<String>> { Vector() }))
                    if (songTitles!!.isEmpty()) {
                        songTitles = getSongTitles()
                    }
                    songList.setListData(songTitles)
                }
            }
        })

        // layout
        this.layout = BorderLayout(SECTION_MARGIN, SECTION_MARGIN)
        val songListPanel = JPanel(MigLayout("wrap 5"))
        songListPanel.add(songSearchInputLabel)
        songListPanel.add(songSearchInput, "span, align left")
        songSearchInput.columns = 20
        songListPanel.add(songList, "span")
        this.add(C.splitH(songListPanel, songViewer, outputViewer), BorderLayout.CENTER)
        val transformPanel = JPanel()
        transformPanel.add(maxLinesLabel)
        transformPanel.add(maxLinesInput)
        transformPanel.add(transformButton)
        this.add(transformPanel, BorderLayout.SOUTH)
    }

    // component init
    private val songList: JList<String>
         get() {
            var songTitles = getSongTitles()
            if (songTitles!!.isEmpty()) {
                songTitles = Vector()
                songTitles.add("Unable to load songs!")
            }
            val songList = JList(songTitles)
            songList.fixedCellHeight = SONG_LIST_FONT_SIZE
            songList.fixedCellWidth = SONG_LIST_WIDTH_PX
            songList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            songList.visibleRowCount = SONG_LIST_HEIGHT_ROW
            Config.subscribe(Config.DIR_DATA) { dataPath: String? ->
                var newSongTitles = getSongTitles(Path.of(dataPath, "songs"), true)
                if (newSongTitles!!.isEmpty()) {
                    newSongTitles = Vector()
                    newSongTitles.add("Unable to load songs!")
                }
                songList.setListData(newSongTitles)
            }
            return songList
        }

    private val songViewer: JTextArea
         get() {
            val textArea = JTextArea()
            textArea.columns = SONG_VIEW_WIDTH_COL
            return textArea
        }

    private val outputViewer: JTextArea
         get() {
            val textArea = JTextArea()
            textArea.columns = OUTPUT_VIEW_WIDTH_COL
            return textArea
        }

    companion object {
        private const val TRANSFORM_BUTTON = "Transform"
        private const val MAX_LINES_INPUT_LABEL = "Lines Per Slide Per Language"
        private const val SONG_SEARCH_INPUT_LABEL = "Search Song"
        private const val SECTION_MARGIN = 10
        private const val SONG_LIST_WIDTH_PX = 400
        private const val SONG_LIST_HEIGHT_ROW = 10
        private const val SONG_LIST_FONT_SIZE = 16
        private const val SONG_VIEW_WIDTH_COL = 30
        private const val OUTPUT_VIEW_WIDTH_COL = 20
        private var songTitles: Vector<String>? = null

        // functions
        private fun getSongTitles(): Vector<String>? {
            return getSongTitles(Path.of(Config.get(Config.DIR_DATA), "songs"), false)
        }

        private fun getSongTitles(dirSongPath: Path, bustCache: Boolean): Vector<String>? {
            if (!bustCache && songTitles != null) {
                return songTitles
            }
            songTitles = try {
                Files.list(dirSongPath)
                        .map { path: Path -> path.fileName.toString() }
                        .filter { path: String -> path.endsWith(".yaml") }
                        .map { path: String -> path.replace(".yaml", "") }
                        .sorted()
                        .collect(Collectors.toCollection(Supplier<Vector<String>> { Vector() }))
            } catch (e: IOException) {
                System.err.println(e.message)
                Vector()
            }
            return songTitles
        }

        private fun loadSong(songList: JList<*>, songViewer: JTextArea) {
            val songName = songList.selectedValue as String
            var songContent = SongYAMLAdapter.getSerializedSong(songName)
            if (songContent == null) {
                songContent = "Error getting content for song $songName"
            }
            songViewer.text = songContent
        }

        private fun transformSong(inputViewer: JTextArea, outputViewer: JTextArea, linesPerSlidePerLang: Int) {
            val song = SongYAMLAdapter.deserialize(inputViewer.text)
            val output = SongSlideTextAdapter.serialize(song, listOf("zh", "en"),
                    linesPerSlidePerLang)
            outputViewer.text = output
        }
    }
}