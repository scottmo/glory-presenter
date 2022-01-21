package com.scottscmo.ppt

import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object Presentation {
    private val bibleTemplateHandler = BibleTemplateHandler()
    private val songTemplateHandler = SongTemplateHandler()

    @Throws(IOException::class)
    fun createFromTemplate(templateFilePath: String, outputFilePath: String) {
        FileInputStream(File(templateFilePath)).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            val slides = ppt.slides
            for (i in slides.indices) {
                bibleTemplateHandler.evaluateTemplate(ppt, slides[i].slideNumber - 1)
                songTemplateHandler.evaluateTemplate(ppt, slides[i].slideNumber - 1)
            }
            FileOutputStream(File(outputFilePath)).use { outStream -> ppt.write(outStream) }
            ppt.close()
        }
    }
}