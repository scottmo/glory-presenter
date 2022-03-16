package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*
import com.scottscmo.util.StringUtils
import org.bouncycastle.cert.ocsp.Req

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
            slideTextConfig: Map<String, SlideTextConfig>, startIndex: Int): List<Request> {
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

    fun createTextBox(textBoxId: String, pageElementId: String,
            w: Double, h: Double, tx: Double, ty: Double): Request {
        return Request().apply {
            createShape = CreateShapeRequest().apply {
                objectId = textBoxId
                shapeType = "TEXT_BOX"
                elementProperties = PageElementProperties().apply {
                    pageObjectId = pageElementId
                    setSize(Size().apply {
                        width = Util.getDimension(w)
                        height = Util.getDimension(h)
                    })
                    transform = AffineTransform().apply {
                        scaleX = 1.0
                        scaleY = 1.0
                        translateX = tx
                        translateY = ty
                        unit = "PT"
                    }
                }
            }
        }
    }

    fun insertText(textBoxId: String, textContent: String, config: SlideTextConfig,
            textInsertionIndex: Int): List<Request> {
        val textInsertRequest = Request()
        val textStyleReqeust = Request()
        // text
        textInsertRequest.apply {
            insertText = InsertTextRequest().apply {
                objectId = textBoxId
                text = textContent
                insertionIndex = textInsertionIndex
            }
        }

        val insertTextRange = Util.getTextRange(textInsertionIndex, textInsertionIndex + textContent.length)

        // paragraph style
        var hasParagraphStyle = false
        val paragraphStyle = ParagraphStyle().apply {
            if (config.alignment.isNotEmpty()) {
                hasParagraphStyle = true
                alignment = config.alignment
            }
            if (config.margin > 0) {
                hasParagraphStyle = true
                val indent = Util.getDimension(config.margin)
                indentFirstLine = indent
                indentStart = indent
                indentEnd = indent
            }
        }
        if (hasParagraphStyle) {
            textStyleReqeust.apply {
                updateParagraphStyle = UpdateParagraphStyleRequest().apply {
                    objectId = textBoxId
                    style = paragraphStyle
                    fields = "*"
                    textRange = insertTextRange
                }
            }
        }

        // text style
        var hasTextStyle = false
        val textStyle = TextStyle().apply {
            if (config.fontStyles.isNotEmpty()) {
                hasTextStyle = true
                smallCaps = config.fontStyles.contains("smallCaps")
                strikethrough = config.fontStyles.contains("strikethrough")
                underline = config.fontStyles.contains("underline")
                bold = config.fontStyles.contains("bold")
                italic = config.fontStyles.contains("italic")
            }
            if (config.fontColor.isNotEmpty()) {
                hasTextStyle = true
                foregroundColor = OptionalColor().apply {
                    opaqueColor = Util.getRGBColor(config.fontColor)
                }
            }
            if (config.fontSize > 0) {
                hasTextStyle = true
                fontSize = Util.getDimension(config.fontSize)
            }
            if (config.fontFamily.isNotEmpty()) {
                hasTextStyle = true
                fontFamily = config.fontFamily
            }
            if (config.fontStyles.contains("bold")) {
                weightedFontFamily = WeightedFontFamily().apply {
                    fontFamily = config.fontFamily
                    weight = 700
                }
            }
        }
        if (hasTextStyle) {
            textStyleReqeust.apply {
                updateTextStyle = UpdateTextStyleRequest().apply {
                    objectId = textBoxId
                    style = textStyle
                    fields = "*"
                    textRange = insertTextRange
                }
            }
        }

        return listOf(textInsertRequest, textStyleReqeust)
    }

    /**
     * use to match slide configuration. hard-coding en, zh for now.
     */
    private fun getLanguage(segment: StringUtils.StringSegment): String {
        return if (segment.isAscii) "en" else "zh"
    }
}