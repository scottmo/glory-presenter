package com.scottscmo.ppt

import com.scottscmo.model.bible.BibleModel
import com.scottscmo.model.bible.BibleReference
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout

class BibleTemplateHandler {
    private val bibleModel: BibleModel = BibleModel.instance
    private val titleMasterKey = "title"
    private val verseMasterKeyPrefix = "verse"
    private val mainLayoutKey = "main"

    /**
     * expression format: e.g. {bible} cuv,niv - john 1:2-3;2:1-2
     */
    fun evaluateTemplate(ppt: XMLSlideShow, index: Int) {
        val srcSlide = ppt.slides[index]
        val bibleReference = TemplatingUtil.findText(srcSlide, "{bible}")
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
        val titleLayout = TemplatingUtil.getSlideMasterLayout(ppt, titleMasterKey, mainLayoutKey)
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
            val key = "${this.verseMasterKeyPrefix}_$version"
            verseLayouts[key] = TemplatingUtil.getSlideMasterLayout(ppt, key, mainLayoutKey)
        }

        val numVerses = bibleVerses[ref.versions[0]]?.size ?: 0
        for (i in 0 until numVerses) {
            for (version in ref.versions) {
                val slide = ppt.createSlide(verseLayouts["${this.verseMasterKeyPrefix}_$version"])
                val verse = bibleVerses[version]!![i]
                val refStr = String.format("%s %d:%d", bookNames[version], verse.chapter, verse.verse)
                TemplatingUtil.replacePlaceholders(slide, mapOf(
                    "{verse}" to verse.verse.toString() + " " + verse.text,
                    "{title}" to refStr
                ))
            }
        }
    }
}