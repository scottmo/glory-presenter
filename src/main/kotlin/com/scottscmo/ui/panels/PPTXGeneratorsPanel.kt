package com.scottscmo.ui.panels

import com.scottscmo.Config
import com.scottscmo.ppt.BibleSlidesGenerator
import com.scottscmo.ppt.PPTXGenerators
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
        val outputDirDefault = FormInput("Output Folder", "directory", Config.getRelativePath("../output"))

        add(Form("PPTX Generator", mapOf(
            dataPathKey to FormInput("Input File", "file", Config.getRelativePath(".")),
            templatePathKey to FormInput("Template File", "file", Config.getRelativePath(".")),
            outputDirKey to outputDirDefault
        )) {
            PPTXGenerators.generate(it[dataPathKey], it[templatePathKey], it[outputDirKey])
            "Slides have been successfully generated!"
        }.ui, "wrap")

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
