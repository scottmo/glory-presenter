package com.scottscmo.ppt

import org.apache.poi.xslf.usermodel.*

object TemplatingUtil {

    fun replaceText(slide: XSLFSlide, replacements: Map<String, String>) {
        slide.shapes
            .filterIsInstance<XSLFTextShape>()
            .forEach { shape -> replaceText(shape, replacements) }
    }

    private fun replaceText(shape: XSLFTextShape, replacements: Map<String, String>) {
        shape.textParagraphs.forEach { pp ->
            var text = pp.text
            for ((searchText, replacement) in replacements) {
                if (text.contains(searchText)) {
                    text = text.replace(searchText, replacement)
                }
            }
            for (textRun in pp.textRuns) {
                if (textRun.rawText != "\n") {
                    textRun.setText("")
                }
            }
            pp.textRuns[0].setText(text)
            val ppxml = pp.xmlObject
            for (i in ppxml.sizeOfBrArray() downTo 1) {
                ppxml.removeBr(i - 1)
            }
        }
    }

    fun replacePlaceholders(slide: XSLFSlide, replacements: Map<String, String>) {
        for (textShape in slide.placeholders) {
            var text = textShape.text
            for ((searchText, replacement) in replacements) {
                if (text.contains(searchText)) {
                    text = text.replace(searchText, replacement)
                }
            }
            clearText(textShape)
            appendText(textShape, text)
        }
    }

    fun appendText(textShape: XSLFTextShape, text: String) {
        val lines = text.trim().split("\n")
        val pps = textShape.textParagraphs
        val pp = pps[pps.size - 1]
        for (i in lines.indices) {
            pp.addNewTextRun().setText(lines[i])
            if (i + 1 < lines.size) {
                pp.addLineBreak()
            }
        }
    }

    /**
     * Helper to remove all texts and new lines.
     * XSLFTextShape.setText("") is bugged when there's new line
     */
    fun clearText(textShape: XSLFTextShape) {
        for (pp in textShape.textParagraphs) {
            for (textRun in pp.textRuns) {
                if (textRun.rawText != "\n") {
                    textRun.setText("")
                }
            }
            val ppxml = pp.xmlObject
            for (i in ppxml.sizeOfBrArray() downTo 1) {
                ppxml.removeBr(i - 1)
            }
        }
    }

    fun replacePlaceholders(slide: XSLFSlide, searchText: String, replacement: String) {
        replacePlaceholders(slide, mapOf(searchText to replacement))
    }

    fun findText(slide: XSLFSlide, searchText: String): String? {
        for (textShape in slide.placeholders) {
            val text = textShape.text
            if (text.contains(searchText)) {
                return text
            }
        }
        return null
    }

    fun getSlideMaster(ppt: XMLSlideShow, name: String): XSLFSlideMaster? {
        return ppt.slideMasters.firstOrNull { it.theme.name == name }
    }

    fun getSlideMasterLayout(ppt: XMLSlideShow, name: String, layout: String): XSLFSlideLayout? {
        return getSlideMaster(ppt, name)?.getLayout(layout)
    }
}