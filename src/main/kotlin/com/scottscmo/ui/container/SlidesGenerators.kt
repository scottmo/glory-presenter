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
        layout = MigLayout()

        add(Form("CSV -> Slides Generator", mapOf(
            "dataFilePath" to FormInput("Input CSV", "text", "input.csv"),
            "headers" to FormInput("Field Names", "text"),
            "tmplFilePath" to FormInput("Template File", "text", "template.pptx"),
            "outputFilePath" to FormInput("Output File Path", "text", "output.pptx")
        )) {
            CSVSlidesGenerator.generate(
                Config.getRelativePath(it["dataFilePath"]), it["headers"].split(",").toTypedArray(),
                Config.getRelativePath(it["tmplFilePath"]), Config.getRelativePath(it["outputFilePath"]))
            "Slides have been successfully generated!"
        }.ui, "wrap")

        add(Form("Bible Slides Generator", mapOf(
            "verses" to FormInput("Verses", "text"),
            "versions" to FormInput("Bible Versions", "text", "cuv,niv"),
            "template" to FormInput("Template File", "text", "template-bible.pptx"),
            "outputDir" to FormInput("Output Folder", "text", "bible_ppt")
        )) {
            BibleSlidesGenerator.generate(it["template"], it["outputDir"], it["versions"], it["verses"])
            "Bible slides have been successfully generated!"
        }.ui)

        add(Form("Song Slides Generator", mapOf(
            "dataFilePath" to FormInput("Input CSV", "text", "input.csv"),
            "outputFilePath" to FormInput("Output File Path", "text", "output.pptx")
        )) {
            CSVSlidesGenerator.generate(
                Config.getRelativePath(it["dataFilePath"]), arrayOf("verse_zh", "verse_en"),
                Config.getRelativePath("template-song.pptx"),
                Config.getRelativePath(it["outputFilePath"]))
            "Slides have been successfully generated!"
        }.ui, "wrap")
    }
}
