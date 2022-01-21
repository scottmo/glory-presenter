package com.scottscmo.ppt

import com.scottscmo.model.bible.BibleModel
import com.scottscmo.model.bible.BibleReference
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout

class BibleTemplateHandler : TemplateHandler {
    private val bibleModel: BibleModel = BibleModel.instance

    /**
     * expression format: e.g. {bible} cuv,niv - john 1:2-3;2:1-2
     */
    override fun evaluateTemplate(ppt: XMLSlideShow, index: Int) {
        val srcSlide = ppt.slides[index]
        val bibleReference = findText(srcSlide, "{bible}")
        if (!bibleReference.isNullOrEmpty()) {
            insertBibleText(ppt, bibleReference.substring(7).trim())
            println("Inserting bible text at " + srcSlide.slideNumber)
        }
    }

    fun insertBibleText(ppt: XMLSlideShow, bibleReferenceStr: String) {
        insertBibleText(ppt, BibleReference(bibleReferenceStr))
    }

    fun insertBibleText(ppt: XMLSlideShow, ref: BibleReference) {
        val bibleVerses = bibleModel.getBibleVerses(ref)
        requireNotNull(bibleVerses) { "Unable to find bible verse with $ref" }
        val bookNames = bibleModel.getBookNames(ref.book)
        requireNotNull(bookNames) { "Unable to query book names with book ${ref.book}" }

        // create title slide
        val titleLayout = getSlideMasterLayout(ppt, "title", "Title Slide")
        requireNotNull(titleLayout) { "Missing title master slide" }

        val titleSlide = ppt.createSlide(titleLayout)
        val titleSlideValues: MutableMap<String, String> = mutableMapOf()
        for (version in ref.versions) {
            titleSlideValues["{title_$version}"] = bookNames.getOrDefault(version, "")
        }
        titleSlideValues["{range}"] = ref.ranges.joinToString { ";" }
        replaceText(titleSlide, titleSlideValues)

        // create verse slides
        val verseLayouts: MutableMap<String, XSLFSlideLayout?> = mutableMapOf()
        for (version in ref.versions) {
            val key = "verse_$version"
            verseLayouts[key] = getSlideMasterLayout(ppt, key, "Title Slide")
        }

        val numVerses = bibleVerses[ref.versions[0]]?.size ?: 0
        for (i in 0 until numVerses) {
            for (version in ref.versions) {
                val slide = ppt.createSlide(verseLayouts["verse_$version"])
                val verse = bibleVerses[version]!![i]
                val refStr = String.format("%s %d:%d", bookNames[version], verse.chapter, verse.verse)
                replaceText(slide, mapOf(
                    "{verse}" to verse.verse.toString() + " " + verse.text,
                    "{title}" to refStr
                ))
            }
        }
    }
}