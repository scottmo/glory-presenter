package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.google.slides.SlidesApiClient
import com.scottscmo.model.bible.BibleReference
import com.scottscmo.model.song.adapters.SongYAMLAdapter
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import net.miginfocom.swing.MigLayout
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class GSlidesPanel : JPanel() {
    private val slidesApiClient = SlidesApiClient()
    private val slideUrlInput = JTextField()
    private val insertionIndexInput = JTextField("0")

    init {
        layout = MigLayout()

        // ppt id
        add(JLabel("Google Slides URL/ID"))
        add(slideUrlInput, "growx, wrap")

        // ppt slide insertion index
        add(JLabel("Insertion Index"))
        add(insertionIndexInput, "growx, wrap")

        // basic actions
        add(JButton("Set Default Title Text").apply {
            addActionListener {
                slidesApiClient.setDefaultTitleText(getPresentationId())
            }
        }, "wrap")

        add(JButton("Set Base Font").apply {
            addActionListener {
                slidesApiClient.setBaseFont(getPresentationId())
            }
        }, "wrap")

        // bible verses
        val versesKey = "verses"
        val versionsKey = "versions"
        add(Form("Bible Slides Generator", mapOf(
            versesKey to FormInput("Verses", "text", "john 1:2-5,7-8"),
            versionsKey to FormInput("Bible Versions", "text", "cuv,niv"),
        )) {
            val bibleRef = BibleReference("${it[versionsKey]} - ${it[versesKey]}")
            slidesApiClient.insertBibleText(getPresentationId(), bibleRef, getInsertionIndex())
            "Bible slides have been successfully generated!"
        }.ui, "span, wrap")

        val songPathKey = "song"
        add(Form("Song Slides Generator", mapOf(
            songPathKey to FormInput("Song", "file", Config.getRelativePath(Config.SONG_SLIDES_DIR)),
        )) {
            val slideSong = Files.readString(Path.of(it[songPathKey]))
            val song = SongYAMLAdapter.deserialize(slideSong)
            if (song != null) {
                slidesApiClient.insertSong(getPresentationId(), song, getInsertionIndex())
                "Song slides have been successfully generated!"
            } else {
                "Unable to generate song slides!"
            }
        }.ui, "span, wrap")
    }

    private fun getPresentationId(): String {
        val input = slideUrlInput.text
        if (input.contains("/")) {
            val url = URL(slideUrlInput.text)
            return url.path.substring(url.path.indexOf("/d/") + 3, url.path.lastIndexOf("/"))
        }
        return input
    }

    private fun getInsertionIndex(): Int {
        return insertionIndexInput.text.toInt()
    }
}
