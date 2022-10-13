package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.google.Action
import com.scottscmo.google.GoogleSlidesService
import com.scottscmo.bibleReference.BibleReference
import com.scottscmo.model.song.converters.KVMDConverter
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
    private val googleService = GoogleSlidesService()
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
                googleService.setDefaultTitleText(getPresentationId())
            }
        }, "wrap")

        add(JButton("Set Base Font").apply {
            addActionListener {
                googleService.setBaseFont(getPresentationId())
            }
        }, "wrap")

        // bible verses
        add(Form("Bible Slides Generator", mapOf(
            "verses"   to FormInput("Verses", "text", "john 1:2-5,7-8"),
            "versions" to FormInput("Bible Versions", "text", "cuv,niv"),
        )) {
            val bibleRef =
                BibleReference("${it["versions"]} - ${it["verses"]}")
            googleService.insertBibleText(getPresentationId(), bibleRef, getInsertionIndex())
            "Bible slides have been successfully generated!"
        }.ui, "span, wrap")

        add(Form("Song Slides Generator", mapOf(
            "song" to FormInput("Song", "filelist", Config.getRelativePath(Config.SONG_SLIDES_DIR)),
        )) {
            val slideSong = Files.readString(Path.of(it["song"]))
            val song = KVMDConverter.parse(slideSong)
            if (song != null) {
                googleService.insertSong(getPresentationId(), song, getInsertionIndex())
                "Song slides have been successfully generated!"
            } else {
                "Unable to generate song slides!"
            }
        }.ui, "span, wrap")

        add(Form("Generate from template", mapOf(
            "title"      to FormInput("Title", "text"),
            "folderId"   to FormInput("Folder ID", "text"),
            "templateId" to FormInput("Template ID", "text"),
            "inserts"    to FormInput("Inserts", "textarea")
        )) {
            try {
                val inserts = parseSlideInserts(it["inserts"])
                val presentationId = googleService.copyPresentation(it["title"], it["folderId"], it["templateId"])
                googleService.generateSlides(presentationId, inserts)
                "Generated!"
            } catch (e :Exception) {
                 e.message!!
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

    private fun parseSlideInserts(inserts: String): List<Action> {
        try {
            return inserts.split("\n").map {insert ->
                val ( index, action ) = insert.split(":", limit=2).map { s -> s.trim() }
                val ( type, input ) = action.split("/", limit=2).map { s -> s.trim() }
                Action(type, index.toInt(), input)
            }
        } catch (e: Exception) {
            throw Exception("Invalid inserts! ${e.message}")
        }
    }
}
