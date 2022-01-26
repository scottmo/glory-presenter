package com.scottscmo.ui.container

import com.scottscmo.ppt.BibleSlidesGenerator
import com.scottscmo.ui.components.Form
import com.scottscmo.ui.components.FormInput
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class SlidesGenerators : JPanel() {
    init {
        layout = MigLayout()

        add(Form("Bible Slides Generator", mapOf(
            "verses" to FormInput("Verses", "text"),
            "versions" to FormInput("Bible Versions", "text", "cuv,niv"),
            "template" to FormInput("Template File", "text", "template-bible.pptx"),
            "outputDir" to FormInput("Output Folder", "text", "bible_ppt")
        )) {
            BibleSlidesGenerator.generate(it["template"], it["outputDir"], it["versions"], it["verses"])
            "Bible slides have been successfully generated!"
        }.ui, "wrap")
    }
}
