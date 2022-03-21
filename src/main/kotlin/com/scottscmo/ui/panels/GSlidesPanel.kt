package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.google.API_CONFIG_DIR
import com.scottscmo.google.CREDENTIALS_FILE_PATH
import com.scottscmo.google.slides.SlidesApiClient
import com.scottscmo.model.bible.BibleReference
import com.scottscmo.ppt.BibleSlidesGenerator
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import com.scottscmo.util.Cryptor
import net.miginfocom.swing.MigLayout
import java.net.URL
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

class GSlidesPanel : JPanel() {
    private val slidesApiClient = SlidesApiClient()
    private val slideUrlInput = JTextField()
    private val insertionIndexInput = JTextField("0")

    init {
        layout = MigLayout()

        add(Form("Google API Credentials Importer", mapOf(
            "credentialsFilePath" to FormInput("Credentials json", "file",
                    Config.getRelativePath("$API_CONFIG_DIR/credentials.json")),
        )) {
            require(Config.get().clientInfoKey.isNotEmpty()) { "clientInfoKey is missing from config.yaml!" }

            Cryptor.encryptFile(it["credentialsFilePath"],
                Config.getRelativePath(CREDENTIALS_FILE_PATH),
                Config.get().clientInfoKey)
            "Credentials imported!"
        }.ui, "wrap")

        add(JLabel("Google Slides URL/ID"))
        add(slideUrlInput, "wrap")

        add(JLabel("Insertion Index"))
        add(insertionIndexInput, "wrap")

        val versesKey = "verses"
        val versionsKey = "versions"
        add(Form("Bible Slides Generator", mapOf(
            versesKey to FormInput("Verses", "text", "john 1:2-5,7-8"),
            versionsKey to FormInput("Bible Versions", "text", "cuv,niv"),
        )) {
            val bibleRef = BibleReference("${it[versionsKey]} - ${it[versesKey]}")
            slidesApiClient.insertBibleText(getPresentationId(), bibleRef, getInsertionIndex())
            "Bible slides have been successfully generated!"
        }.ui, "wrap")
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
