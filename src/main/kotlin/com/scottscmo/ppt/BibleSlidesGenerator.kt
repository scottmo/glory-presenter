package com.scottscmo.ppt

import com.scottscmo.model.bible.BibleMetadata
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object BibleSlidesGenerator {
    private val bibleTemplateHandler = BibleTemplateHandler()

    @Throws(IOException::class)
    fun insertBibleText(templateFilePath: String, outputFilePath: String, bibleReference: String) {
        FileInputStream(templateFilePath).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            bibleTemplateHandler.insertBibleText(ppt, bibleReference)
            ppt.removeSlide(0)
            FileOutputStream(outputFilePath).use { outStream -> ppt.write(outStream) }
            ppt.close()
        }
    }

    @Throws(IOException::class)
    fun generateSlides(templatePath: String, destDir: String, versions: String) {
        BibleMetadata.bookInfoMap.forEach {
            for (i in it.value.count.indices) {
                val chapter = it.key + " " + (i + 1)
                insertBibleText(templatePath, "$destDir/$chapter.pptx", "$versions - $chapter")
            }
        }
    }
}

@Throws(IOException::class)
fun main() {
    val templatePath = "template-bible.pptx"
    BibleSlidesGenerator.generateSlides(templatePath, "bible_ppt", "cuv,niv")
}