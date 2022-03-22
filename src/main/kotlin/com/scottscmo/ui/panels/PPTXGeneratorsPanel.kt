package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.ppt.BibleSlidesGenerator
import com.scottscmo.ppt.CSVSlidesGenerator
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class PPTXGeneratorsPanel : JPanel() {
    init {
        layout = MigLayout()

        // common fields
        val dataPathKey = "dataFilePath"
        val templatePathKey = "tmplFilePath"
        val outputDirKey = "outputDirPath"

        // common field defaults
        val dataPathDefault = FormInput("Input CSV", "file", Config.getRelativePath("input.csv"))
        val outputDirDefault = FormInput("Output Folder", "directory", Config.getRelativePath("../output"))

        val headersKey = "headers"
        add(Form("CSV -> Slides Generator", mapOf(
            dataPathKey to dataPathDefault,
            headersKey to FormInput("Field Names", "text"),
            templatePathKey to FormInput("Template File", "file", Config.getRelativePath("template.pptx")),
            outputDirKey to outputDirDefault
        )) {
            CSVSlidesGenerator.generate(it[dataPathKey], it[headersKey].split(","),
                it[templatePathKey], it[outputDirKey])
            "Slides have been successfully generated!"
        }.ui, "wrap")

        add(Form("Song Slides Generator", mapOf(
            dataPathKey to dataPathDefault,
            templatePathKey to FormInput("Template File", "file", Config.getRelativePath("template-song.pptx")),
            outputDirKey to outputDirDefault
        )) {
            CSVSlidesGenerator.generate(it[dataPathKey], listOf("verse_zh", "verse_en"), it[templatePathKey],
                it[outputDirKey])
            "Slides have been successfully generated!"
        }.ui)

        val versesKey = "verses"
        val versionsKey = "versions"
        add(Form("Bible Slides Generator", mapOf(
            versesKey to FormInput("Verses", "text", "john 1:2-5,7-8"),
            versionsKey to FormInput("Bible Versions", "text", "cuv,niv"),
            templatePathKey to FormInput("Template File", "file", Config.getRelativePath("template-bible.pptx")),
            outputDirKey to outputDirDefault
        )) {
            BibleSlidesGenerator.generate(it[templatePathKey], it[outputDirKey], it[versionsKey], it[versesKey])
            "Bible slides have been successfully generated!"
        }.ui, "wrap")
    }
}
