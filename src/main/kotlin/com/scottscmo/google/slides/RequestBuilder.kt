package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*
import com.scottscmo.ParagraphConfig
import com.scottscmo.SlideConfig
import com.scottscmo.TextConfig
import com.scottscmo.util.StringUtils

class RequestBuilder {
    private val requests = mutableListOf<Request>()

    fun build(): List<Request> {
        return requests
    }

    /**
     * set base font for a slide
     */
    fun setBaseFont(slide: Page, textConfigs: Map<String, TextConfig>) {
        slide.pageElements
            .filter { it.objectId != null }
            .forEach { pageElement ->
                Util.getTextElements(pageElement)
                    .filter { !it.textRun.isNullOrEmpty() }
                    .forEach { textElement ->
                        setBaseFontForText(pageElement.objectId, textElement.textRun,
                            textConfigs, textElement.startIndex)
                    }
        }
    }

    /**
     * set base font for a text run
     */
    private fun setBaseFontForText(pageElementId: String, textRun: TextRun,
            textConfigs: Map<String, TextConfig>, startIndex: Int) {
        textRun.content?.let { content ->
            StringUtils.splitByCharset(content, true).forEach { contentSegment ->
                val textConfigName = getTextConfigName(contentSegment)
                requests.add(Request().apply {
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
                })
            }
        }
    }

    fun resizeToFullPage(pageElementId: String) {
        requests.add(Request().apply {
            updatePageElementTransform = UpdatePageElementTransformRequest().apply {
                objectId = pageElementId
                transform = AffineTransform().apply {
                    scaleX = DefaultSlideConfig.SLIDE_W / DefaultSlideConfig.SLIDE_BASE
                    scaleY = DefaultSlideConfig.SLIDE_H / DefaultSlideConfig.SLIDE_BASE
                    unit = "PT"
                }
                applyMode = "ABSOLUTE"
            }
        })
    }

    fun createTextBox(pageElementId: String,
            w: Double, h: Double, tx: Double, ty: Double): String {
        val textBoxId = Util.generateObjectId(DefaultSlideConfig.ID_SHAPE_PREFIX)
        return createTextBox(textBoxId, pageElementId, w, h, tx, ty)
    }

    private fun createTextBox(textBoxId: String, pageElementId: String,
            w: Double, h: Double, tx: Double, ty: Double): String {
        requests.add(Request().apply {
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
        })
        return textBoxId
    }

    fun insertText(textBoxId: String, textContent: String,
            paragraphConfig: ParagraphConfig, textConfig: TextConfig,
            textInsertionIndex: Int = 0) {
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
    }

    /**
     * Insert multilingual texts.
     * @param textBoxId - objectId/pageElementId for the text box
     * @param texts - lang to text map
     * @param langs - languages to use. skip if there's no text in that language
     * @return list of update requests
     */
    fun insertText(textBoxId: String, textConfig: Map<String, String>, slideConfig: SlideConfig) {
        // we always insert from the top of the text box, so reverse the list and when inserting,
        // we push the text down
        slideConfig.textConfigsOrder.reversed()
            .filter { configName -> textConfig.containsKey(configName) }
            .forEachIndexed { index, configName ->
                val ln = if (index == 0) "" else "\n"
                insertText(textBoxId, textConfig[configName]!! + ln,
                        slideConfig.paragraph, slideConfig.textConfigs[configName]!!)
            }
    }

    fun createText(pageElementId: String, textContent: String,
        paragraphConfig: ParagraphConfig, textConfig: TextConfig, isFullPage: Boolean): String {
        val textBoxW = DefaultSlideConfig.SLIDE_W
        val textBoxH = if (isFullPage || textConfig.fontSize <= 0) DefaultSlideConfig.SLIDE_H
                else textConfig.fontSize * 2

        val textBoxId = createTextBox(pageElementId, textBoxW, textBoxH, paragraphConfig.x, paragraphConfig.y)
        insertText(textBoxId, textContent, paragraphConfig, textConfig)
        return textBoxId
    }

    private fun copyText(srcElement: PageElement, dstElement: PageElement, withStyles: Boolean) {
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
            return
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
    }

    private fun copyShape(srcElement: PageElement, dstElement: PageElement) {
        srcElement.shape?.shapeProperties?.let { srcShapeProperties ->
            requests.add(Request().apply {
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
            })
        }
    }

    private fun copyTransform(srcElement: PageElement, dstElement: PageElement) {
        requests.add(Request().apply {
            updatePageElementTransform = UpdatePageElementTransformRequest().apply {
                objectId = dstElement.objectId
                transform = srcElement.transform
                applyMode = "ABSOLUTE"
            }
        })
    }

    private fun deleteObject(id: String) {
        requests.add(Request().apply {
            deleteObject = DeleteObjectRequest().apply {
                objectId = id
            }
        })
    }

    fun createSlide(slideIndex: Int): String {
        val slideId = Util.generateObjectId(DefaultSlideConfig.ID_SLIDE_PREFIX)
        return this.createSlide(slideIndex, slideId)
    }

    private fun createSlide(slideIndex: Int, slideId: String): String {
        requests.add(Request().apply {
            createSlide = CreateSlideRequest().apply {
                objectId = slideId
                insertionIndex = slideIndex
                slideLayoutReference = LayoutReference().apply {
                    predefinedLayout = "TITLE_ONLY"
                }
                placeholderIdMappings = listOf(LayoutPlaceholderIdMapping().apply {
                    objectId = getPlaceHolderId(slideId)
                    layoutPlaceholder = Placeholder().apply {
                        type = "TITLE"
                    }
                })
            }
        })
        return slideId
    }

    fun getPlaceHolderId(slideId: String): String {
        return DefaultSlideConfig.ID_PLACEHOLDER_PREFIX + "-" + slideId
    }

    /**
     * Create a slide with title text resized to full slide and return its id
     * @return text box objectId
     */
    fun createSlideWithFullText(slideIndex: Int): String {
        val slideId = this.createSlide(slideIndex)
        val titleId = this.getPlaceHolderId(slideId)
        this.resizeToFullPage(titleId)
        return titleId
    }

    fun setDefaultTitleText(slide: Page) {
        val firstText = Util.getFirstText(slide)
        val title = Util.getTitlePlaceholder(slide)

        if (title == null || firstText?.objectId == null || firstText == title) {
            return
        }

        this.copyText(firstText, title, true)
        this.copyShape(firstText, title)
        this.copyTransform(firstText, title)
        this.deleteObject(firstText.objectId)
    }

    /**
     * use to match slide configuration. hard-coding en, zh for now.
     */
    private fun getTextConfigName(segment: StringUtils.StringSegment): String {
        return if (segment.isAscii) "en" else "zh"
    }
}