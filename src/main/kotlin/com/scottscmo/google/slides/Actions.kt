package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*
import com.scottscmo.util.StringUtils

object Actions {
    /**
     * set base font for a slide
     */
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

    /**
     * set base font for a text run
     */
    private fun setBaseFontForText(pageElementId: String, textRun: TextRun,
            slideTextConfig: Map<String, SlideTextConfig>, startIndex: Int = 0): List<Request> {
        if (textRun.content.isNullOrEmpty()) return emptyList()

        return StringUtils.splitByCharset(textRun.content, true)
            .map { contentSegment ->
            val lang = getLanguage(contentSegment)
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

    fun resizeToFullPage(pageElementId: String): Request {
        return Request().apply {
            updatePageElementTransform = UpdatePageElementTransformRequest().apply {
                objectId = pageElementId
                transform = AffineTransform().apply {
                    scaleX = DefaultSlideConfig.SLIDE_W / DefaultSlideConfig.SLIDE_BASE
                    scaleY = DefaultSlideConfig.SLIDE_H / DefaultSlideConfig.SLIDE_BASE
                    unit = "PT"
                }
                applyMode = "ABSOLUTE"
            }
        }
    }

    /**
     * use to match slide configuration. hard-coding en, zh for now.
     */
    private fun getLanguage(segment: StringUtils.StringSegment): String {
        return if (segment.isAscii) "en" else "zh"
    }
}