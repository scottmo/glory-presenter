package com.scottscmo.ppt

import com.scottscmo.Config
import com.scottscmo.Config.DIR_DATA
import com.scottscmo.model.bible.BibleMetadata
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object BibleSlidesGenerator {
    private val bibleTemplateHandler = BibleTemplateHandler()

    @Throws(IOException::class)
    private fun insertBibleText(templateFilePath: String, outputFilePath: String, bibleReference: String) {
        FileInputStream(templateFilePath).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            bibleTemplateHandler.insertBibleText(ppt, bibleReference)
            ppt.removeSlide(0)
            FileOutputStream(outputFilePath).use { outStream -> ppt.write(outStream) }
            ppt.close()
        }
    }

    @Throws(IOException::class)
    fun generate(templatePath: String, destDir: String, versions: String, verses: String = "") {
        if (verses.isEmpty()) {
            BibleMetadata.bookInfoMap.forEach {
                for (i in it.value.count.indices) {
                    val chapter = it.key + " " + (i + 1)
                    insertBibleText("${Config[DIR_DATA]}/$templatePath",
                        "${Config[DIR_DATA]}/$destDir/$chapter.pptx", "$versions - $chapter")
                }
            }
        } else {
            val bibleReference = "$versions - $verses"
            insertBibleText("${Config[DIR_DATA]}/$templatePath",
                "${Config[DIR_DATA]}/$destDir/$bibleReference.pptx", bibleReference)
        }
    }
}
