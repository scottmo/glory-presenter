package com.scottscmo.ppt

import com.scottscmo.Config
import com.scottscmo.model.bible.BibleMetadata
import com.scottscmo.model.bible.BibleModel
import com.scottscmo.bibleReference.BibleReference
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object BibleSlidesGenerator {
    private const val TITLE_MASTER_KEY = "title"
    private const val VERSE_MASTER_KEY_PREFIX = "verse"
    private const val MAIN_LAYOUT_KEY = "main"

    private val bibleModel: BibleModel = BibleModel.get()

    @Throws(IOException::class)
    private fun insertBibleText(templateFilePath: String, outputFilePath: String, bibleReference: String) {
        FileInputStream(templateFilePath).use { inStream ->
            val ppt = XMLSlideShow(inStream)
            insertBibleText(ppt, bibleReference)
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
                    insertBibleText(Config.getRelativePath(templatePath),
                        Config.getRelativePath("$destDir/$chapter.pptx"), "$versions - $chapter")
                }
            }
        } else {
            val bibleReference = "$versions - $verses"
            insertBibleText(Config.getRelativePath(templatePath),
                Config.getRelativePath("$destDir/$bibleReference.pptx"), bibleReference)
        }
    }

    private fun insertBibleText(ppt: XMLSlideShow, bibleReferenceStr: String) {
        insertBibleText(ppt, BibleReference(bibleReferenceStr))
    }

    private fun insertBibleText(ppt: XMLSlideShow, ref: BibleReference) {
        val bibleVerses = bibleModel.getBibleVerses(ref)
        val bookNames = bibleModel.getBookNames(ref.book)
        requireNotNull(bookNames) { "Unable to query book names with book ${ref.book}" }

        // create title slide
        val titleLayout = TemplatingUtil.getSlideMasterLayout(ppt, TITLE_MASTER_KEY, MAIN_LAYOUT_KEY)
        requireNotNull(titleLayout) { "Missing title master slide" }

        val titleSlide = ppt.createSlide(titleLayout)
        val titleSlideValues: MutableMap<String, String> = mutableMapOf()
        for (version in ref.versions) {
            titleSlideValues["{title_$version}"] = bookNames.getOrDefault(version, "")
        }
        titleSlideValues["{range}"] = ref.ranges.joinToString(";")
        TemplatingUtil.replacePlaceholders(titleSlide, titleSlideValues)

        // create verse slides
        val verseLayouts: MutableMap<String, XSLFSlideLayout?> = mutableMapOf()
        for (version in ref.versions) {
            val key = "${this.VERSE_MASTER_KEY_PREFIX}_$version"
            verseLayouts[key] = TemplatingUtil.getSlideMasterLayout(ppt, key, MAIN_LAYOUT_KEY)
        }

        val numVerses = bibleVerses[ref.versions[0]]?.size ?: 0
        for (i in 0 until numVerses) {
            for (version in ref.versions) {
                val slide = ppt.createSlide(verseLayouts["${this.VERSE_MASTER_KEY_PREFIX}_$version"])
                val verse = bibleVerses[version]!![i]
                val refStr = String.format("%s %d:%d", bookNames[version], verse.chapter, verse.index)
                TemplatingUtil.replacePlaceholders(slide, mapOf(
                    "{verse}" to verse.index.toString() + " " + verse.text,
                    "{title}" to refStr
                ))
            }
        }
    }
}
