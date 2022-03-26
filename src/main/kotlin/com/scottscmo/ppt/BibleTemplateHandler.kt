package com.scottscmo.ppt

import com.scottscmo.model.bible.BibleModel
import com.scottscmo.model.bible.BibleReference
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFSlideLayout

class BibleTemplateHandler {
    private val bibleModel: BibleModel = BibleModel.get()
    private val titleMasterKey = "title"
    private val verseMasterKeyPrefix = "verse"
    private val mainLayoutKey = "main"

    fun insertBibleText(ppt: XMLSlideShow, bibleReferenceStr: String) {
        insertBibleText(ppt, BibleReference(bibleReferenceStr))
    }

    fun insertBibleText(ppt: XMLSlideShow, ref: BibleReference) {
        val bibleVerses = bibleModel.getBibleVerses(ref)
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
                val refStr = String.format("%s %d:%d", bookNames[version], verse.chapter, verse.index)
                TemplatingUtil.replacePlaceholders(slide, mapOf(
                    "{verse}" to verse.index.toString() + " " + verse.text,
                    "{title}" to refStr
                ))
            }
        }
    }
}