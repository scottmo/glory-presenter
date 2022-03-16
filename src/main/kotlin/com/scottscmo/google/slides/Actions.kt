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

    fun insertText(textBoxId: String, textContent: String, config: SlideTextConfig,
            textInsertionIndex: Int = 0): List<Request> {
        val textInsertRequest = Request()
        val textStyleRequest = Request()

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
            textStyleRequest.apply {
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
            textStyleRequest.apply {
                updateTextStyle = UpdateTextStyleRequest().apply {
                    objectId = textBoxId
                    style = textStyle
                    fields = "*"
                    textRange = insertTextRange
                }
            }
        }

        return listOf(textInsertRequest, textStyleRequest)
    }

    fun createText(textBoxId: String, pageElementId: String, textContent: String,
            config: SlideTextConfig, isFullPage: Boolean): List<Request> {
        val requests = mutableListOf<Request>()

        val textBoxW = DefaultSlideConfig.SLIDE_W
        val textBoxH = if (isFullPage || config.fontSize <= 0) DefaultSlideConfig.SLIDE_H
                else config.fontSize * 2
        requests.add(createTextBox(textBoxId, pageElementId, textBoxW, textBoxH, config.x, config.y))

        requests.addAll(insertText(textBoxId, textContent, config))

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
    private fun getLanguage(segment: StringUtils.StringSegment): String {
        return if (segment.isAscii) "en" else "zh"
    }
}