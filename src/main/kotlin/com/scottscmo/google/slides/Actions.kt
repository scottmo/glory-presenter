package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*
import com.scottscmo.ParagraphConfig
import com.scottscmo.SlideConfig
import com.scottscmo.TextConfig
import com.scottscmo.util.StringUtils

object Actions {
    /**
     * set base font for a slide
     */
    fun setBaseFont(slide: Page, textConfigs: Map<String, TextConfig>): List<Request> {
        return slide.pageElements
            .filter { it.objectId != null }
            .map { pageElement ->
                Util.getTextElements(pageElement).filter { !it.textRun.isNullOrEmpty() }
                    .map { textElement ->
                        setBaseFontForText(pageElement.objectId, textElement.textRun,
                            textConfigs, textElement.startIndex)
                    }.flatten()
            }.flatten()
    }

    /**
     * set base font for a text run
     */
    private fun setBaseFontForText(pageElementId: String, textRun: TextRun,
            textConfigs: Map<String, TextConfig>, startIndex: Int): List<Request> {
        if (textRun.content.isNullOrEmpty()) return emptyList()

        return StringUtils.splitByCharset(textRun.content, true)
            .map { contentSegment ->
            val textConfigName = getTextConfigName(contentSegment)
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
                            opaqueColor = Util.getRGBColor(textConfigs[textConfigName]?.fontColor)
                        }
                        fontFamily = textConfigs[textConfigName]?.fontFamily
                        weightedFontFamily = textRun.style.weightedFontFamily.clone().apply {
                            fontFamily = textConfigs[textConfigName]?.fontFamily
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

    private fun createTextBox(textBoxId: String, pageElementId: String,
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

    fun insertText(textBoxId: String, textContent: String,
            paragraphConfig: ParagraphConfig, textConfig: TextConfig,
            textInsertionIndex: Int = 0): List<Request> {
        val requests = mutableListOf<Request>()

        // text
        requests.add(Request().apply {
            insertText = InsertTextRequest().apply {
                objectId = textBoxId
                text = textContent
                insertionIndex = textInsertionIndex
            }
        })

        val insertTextRange = Util.getTextRange(textInsertionIndex, textInsertionIndex + textContent.length)

        // paragraph style
        var hasParagraphStyle = false
        val paragraphStyle = ParagraphStyle().apply {
            if (paragraphConfig.alignment.isNotEmpty()) {
                hasParagraphStyle = true
                alignment = paragraphConfig.alignment
            }
            if (paragraphConfig.indentation > 0) {
                hasParagraphStyle = true
                val indent = Util.getDimension(paragraphConfig.indentation)
                indentFirstLine = indent
                indentStart = indent
                indentEnd = indent
            }
        }
        if (hasParagraphStyle) {
            requests.add(Request().apply {
                updateParagraphStyle = UpdateParagraphStyleRequest().apply {
                    objectId = textBoxId
                    style = paragraphStyle
                    fields = "*"
                    textRange = insertTextRange
                }
            })
        }

        // text style
        var hasTextStyle = false
        val textStyle = TextStyle().apply {
            if (textConfig.fontStyles.isNotEmpty()) {
                hasTextStyle = true
                smallCaps = textConfig.fontStyles.contains("smallCaps")
                strikethrough = textConfig.fontStyles.contains("strikethrough")
                underline = textConfig.fontStyles.contains("underline")
                bold = textConfig.fontStyles.contains("bold")
                italic = textConfig.fontStyles.contains("italic")
            }
            if (textConfig.fontColor.isNotEmpty()) {
                hasTextStyle = true
                foregroundColor = OptionalColor().apply {
                    opaqueColor = Util.getRGBColor(textConfig.fontColor)
                }
            }
            if (textConfig.fontSize > 0) {
                hasTextStyle = true
                fontSize = Util.getDimension(textConfig.fontSize)
            }
            if (textConfig.fontFamily.isNotEmpty()) {
                hasTextStyle = true
                fontFamily = textConfig.fontFamily
            }
            if (textConfig.fontStyles.contains("bold")) {
                weightedFontFamily = WeightedFontFamily().apply {
                    fontFamily = textConfig.fontFamily
                    weight = 700
                }
            }
        }
        if (hasTextStyle) {
            requests.add(Request().apply {
                updateTextStyle = UpdateTextStyleRequest().apply {
                    objectId = textBoxId
                    style = textStyle
                    fields = "*"
                    textRange = insertTextRange
                }
            })
        }

        return requests
    }

    /**
     * Insert multilingual texts.
     * @param textBoxId - objectId/pageElementId for the text box
     * @param texts - lang to text map
     * @param langs - languages to use. skip if there's no text in that language
     * @return list of update requests
     */
    fun insertText(textBoxId: String, texts: Map<String, String>, langs: List<String>,
            slideConfig: SlideConfig): List<Request> {
        // we always insert from the top of the text box, so reverse the list and when inserting,
        // we push the text down
        return langs.reversed()
            .filter { lang -> texts.containsKey(lang) }
            .map { lang ->
                Actions.insertText(textBoxId, texts[lang]!!, slideConfig.paragraph, slideConfig.textConfigs[lang]!!)
            }.flatten()
    }

    fun createText(textBoxId: String, pageElementId: String, textContent: String,
        paragraphConfig: ParagraphConfig, textConfig: TextConfig, isFullPage: Boolean): List<Request> {
        val requests = mutableListOf<Request>()

        val textBoxW = DefaultSlideConfig.SLIDE_W
        val textBoxH = if (isFullPage || textConfig.fontSize <= 0) DefaultSlideConfig.SLIDE_H
                else textConfig.fontSize * 2
        requests.add(createTextBox(textBoxId, pageElementId, textBoxW, textBoxH, paragraphConfig.x, paragraphConfig.y))

        requests.addAll(insertText(textBoxId, textContent, paragraphConfig, textConfig))

        return requests
    }

    private fun copyText(srcElement: PageElement, dstElement: PageElement, withStyles: Boolean): List<Request> {
        val requests = mutableListOf<Request>()

        val pageElementId = dstElement.objectId

        srcElement.shape.text.textElements
            .filter { it.textRun?.content?.isNotEmpty() ?: false }
            .forEach {
                requests.add(Request().apply { 
                    insertText = InsertTextRequest().apply { 
                        objectId = pageElementId
                        text = it.textRun.content
                        insertionIndex = it.startIndex
                    }
                })
            }
        
        if (!withStyles) {
            return requests
        }

        srcElement.shape.text.textElements
            .forEach {
                if (it.paragraphMarker != null) {
                    requests.add(Request().apply {
                        updateParagraphStyle = UpdateParagraphStyleRequest().apply {
                            objectId = pageElementId
                            style = it.paragraphMarker.style
                            fields = "*"
                            textRange = Util.getTextRange(it.startIndex, it.endIndex)
                        }
                    })
                } else {
                    requests.add(Request().apply {
                        updateTextStyle = UpdateTextStyleRequest().apply {
                            objectId = pageElementId
                            style = it.textRun.style
                            fields = "*"
                            textRange = Util.getTextRange(it.startIndex, it.endIndex)
                        }
                    })
                }
            }

        return requests
    }

    private fun copyShape(srcElement: PageElement, dstElement: PageElement): Request {
        val srcShapeProperties = srcElement.shape?.shapeProperties ?: return Request()

        return Request().apply {
            updateShapeProperties = UpdateShapePropertiesRequest().apply {
                objectId = dstElement.objectId
                shapeProperties = ShapeProperties().apply {
                    contentAlignment = srcShapeProperties.contentAlignment
                    link = srcShapeProperties.link
                    if (srcShapeProperties.outline.propertyState != "NOT_RENDERED") {
                        outline = srcShapeProperties.outline
                    }
                    if (srcShapeProperties.shapeBackgroundFill.propertyState != "NOT_RENDERED") {
                        shapeBackgroundFill = srcShapeProperties.shapeBackgroundFill
                    }
                    // skip autofit and shadow
                }
            }
        }
    }

    private fun copyTransform(srcElement: PageElement, dstElement: PageElement): Request {
        return Request().apply {
            updatePageElementTransform = UpdatePageElementTransformRequest().apply {
                objectId = dstElement.objectId
                transform = srcElement.transform
                applyMode = "ABSOLUTE"
            }
        }
    }

    private fun deleteObject(id: String): Request {
        return Request().apply {
            deleteObject = DeleteObjectRequest().apply {
                objectId = id
            }
        }
    }

    fun createSlide(slideId: String, slideIndex: Int): Request {
        return Request().apply {
            createSlide = CreateSlideRequest().apply {
                objectId = slideId
                insertionIndex = slideIndex
                slideLayoutReference = LayoutReference().apply {
                    predefinedLayout = "TITLE_ONLY"
                }
                placeholderIdMappings = listOf(LayoutPlaceholderIdMapping().apply {
                    objectId = DefaultSlideConfig.ID_PLACEHOLDER_PREFIX + "-" + slideId
                    layoutPlaceholder = Placeholder().apply {
                        type = "TITLE"
                    }
                })
            }
        }
    }

    fun setDefaultTitleText(slide: Page): List<Request> {
        val firstText = Util.getFirstText(slide)
        val title = Util.getTitlePlaceholder(slide)

        if (title == null || firstText?.objectId == null || firstText == title) {
            return emptyList()
        }

        val requests = mutableListOf<Request>()

        requests.addAll(copyText(firstText, title, true))
        requests.add(copyShape(firstText, title))
        requests.add(copyTransform(firstText, title))
        requests.add(deleteObject(firstText.objectId))

        return requests
    }

    /**
     * use to match slide configuration. hard-coding en, zh for now.
     */
    private fun getTextConfigName(segment: StringUtils.StringSegment): String {
        return if (segment.isAscii) "en" else "zh"
    }
}