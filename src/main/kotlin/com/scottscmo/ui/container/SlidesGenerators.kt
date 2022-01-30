package com.scottscmo.ui.container

import com.scottscmo.Config
import com.scottscmo.ppt.BibleSlidesGenerator
import com.scottscmo.ppt.CSVSlidesGenerator
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SlidesGenerators : JPanel() {
    init {
        val inputCSVConfig = FormInput("Input CSV", "file", Config.getRelativePath("input.csv"))
        val outputDirConfig = FormInput("Output Folder", "directory", Config.getRelativePath("../output"))

        layout = MigLayout()

        add(Form("CSV -> Slides Generator", mapOf(
            "dataFilePath" to inputCSVConfig,
            "headers" to FormInput("Field Names", "text"),
            "tmplFilePath" to FormInput("Template File", "file", Config.getRelativePath("template.pptx")),
            "outputDirPath" to outputDirConfig
        )) {
            CSVSlidesGenerator.generate(it["dataFilePath"], it["headers"].split(","),
                it["tmplFilePath"], it["outputDirPath"])
            "Slides have been successfully generated!"
        }.ui, "wrap")

        add(Form("Song Slides Generator", mapOf(
            "dataFilePath" to inputCSVConfig,
            "tmplFilePath" to FormInput("Template File", "file", Config.getRelativePath("template-song.pptx")),
            "outputDirPath" to outputDirConfig
        )) {
            CSVSlidesGenerator.generate(it["dataFilePath"], listOf("verse_zh", "verse_en"), it["tmplFilePath"],
                it["outputFilePath"])
            "Slides have been successfully generated!"
        }.ui)

        add(Form("Bible Slides Generator", mapOf(
            "verses" to FormInput("Verses", "text"),
            "versions" to FormInput("Bible Versions", "text", "cuv,niv"),
            "tmplFilePath" to FormInput("Template File", "file", Config.getRelativePath("template-bible.pptx")),
            "outputDirPath" to outputDirConfig
        )) {
            BibleSlidesGenerator.generate(it["tmplFilePath"], it["outputDirPath"], it["versions"], it["verses"])
            "Bible slides have been successfully generated!"
        }.ui, "wrap")
    }
}
