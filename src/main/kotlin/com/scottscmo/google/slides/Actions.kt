package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*
import com.scottscmo.util.StringUtils

object Actions {
    fun setBaseFont(slide: Page, slideTextConfig: Map<String, SlideTextConfig>): List<Request> {
        return slide.pageElements
            .filter { it.objectId != null }
            .map { pageElement ->
                Util.getTextElements(pageElement).filter { !it.textRun.isNullOrEmpty() }
                    .map { textElement ->
                        setBaseFontForText(pageElement.objectId, textElement.textRun,
                            slideTextConfig, textElement.startIndex)
                    }.flatten()
            }.flatten()
    }

    private fun setBaseFontForText(pageElementId: String, textRun: TextRun,
            slideTextConfig: Map<String, SlideTextConfig>, startIndex: Int = 0): List<Request> {
        if (textRun.content.isNullOrEmpty()) return emptyList()

        return StringUtils.splitByCharset(textRun.content, true)
            .map { contentSegment ->
            val lang = if (contentSegment.isAscii) "en" else "zh"
            Request().apply {
                updateTextStyle = UpdateTextStyleRequest().apply {
                    objectId = pageElementId
                    fields = "*"
                    textRange = Util.getTextRange(
                        startIndex + contentSegment.startIndex,
                        startIndex + contentSegment.endIndex
                    )
                    style = textRun.style.clone().apply {
                        foregroundColor = OptionalColor().apply {
                            opaqueColor = Util.getRGBColor(slideTextConfig[lang]?.fontColor)
                        }
                        fontFamily = slideTextConfig[lang]?.fontFamily
                        weightedFontFamily = textRun.style.weightedFontFamily.clone().apply {
                            fontFamily = slideTextConfig[lang]?.fontFamily
                        }
                    }
                }
            }
        }
    }
}