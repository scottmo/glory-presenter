package com.scottscmo.ui.panels

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.scottscmo.Config
import com.scottscmo.model.bible.BibleModel
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import java.io.File
import javax.swing.JPanel

class BibleInfoPanel : JPanel() {
    private val mapper = ObjectMapper()
    private val bibleJsonTypeRef = object : TypeReference<Map<String, List<List<String>>>>() {}
    init {
        val dataPathKey = "dataFilePath"
        val versionKey = "version"
        add(Form("Bible Importer", mapOf(
            dataPathKey to FormInput("Input JSON", "file", Config.getRelativePath("bible.json")),
            versionKey to FormInput("Field Names", "text", "niv")
        )) {
            val bibleJson = mapper.readValue(File(it[dataPathKey]), bibleJsonTypeRef)
            val insertedVerseCount = BibleModel.instance.insert(bibleJson, it[versionKey])
            "Successfully inserted $insertedVerseCount ${it[versionKey]} bible verses"
        }.ui)
    }
}