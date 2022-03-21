package com.scottscmo.ui.panels

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.scottscmo.Config
import com.scottscmo.model.bible.BibleMetadata
import com.scottscmo.model.bible.BibleModel
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import net.miginfocom.swing.MigLayout
import java.io.File
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class BibleInfoPanel : JPanel() {
    private val mapper = ObjectMapper()
    private val bibleJsonTypeRef = object : TypeReference<Map<String, List<List<String>>>>() {}
    init {
        layout = MigLayout("", "left", "top")

        val availableVersions = BibleModel.get().getAvailableVersions()
        add(JLabel("Available versions: ${availableVersions.joinToString(", ")}"),
            "wrap")

        val bookIds = BibleMetadata.getBookIdsInOrder().joinToString(", ")
        add(JTextArea("Book IDs: $bookIds").apply {
            columns = 100
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
        }, "span, wrap")

        val dataPathKey = "dataFilePath"
        val versionKey = "version"
        add(Form("Bible Importer", mapOf(
            dataPathKey to FormInput("Input JSON", "file", Config.getRelativePath("bible.json")),
            versionKey to FormInput("Version", "text", "niv")
        )) {
            val bibleJson = mapper.readValue(File(it[dataPathKey]), bibleJsonTypeRef)
            val insertedVerseCount = BibleModel.get().insert(bibleJson, it[versionKey])
            "Successfully inserted $insertedVerseCount ${it[versionKey]} bible verses"
        }.ui, "wrap")
    }
}
