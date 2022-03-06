package com.scottscmo.google.slides

import com.google.api.services.slides.v1.model.*

object Util {
    fun getTextElements(element: PageElement): List<TextElement> {
        return element.shape?.text?.textElements ?: emptyList()
    }

    fun getTitlePlaceholder(slide: Page): PageElement? {
        return slide.pageElements?.find { it.shape?.placeholder?.type === "TITLE" }
    }

    fun getFirstText(slide: Page): PageElement? {
        return slide.pageElements?.find { getTextElements(it).isNotEmpty() }
    }

    fun getTextRange(startIndex: Int, endIndex: Int = 0): Range {
        return if (endIndex != 0) {
            Range().apply {
                this.type = "FIXED_RANGE"
                this.startIndex = startIndex
                this.endIndex = endIndex
            }
        } else {
            Range().apply {
                this.type = "FROM_START_INDEX"
                this.startIndex = startIndex
            }
        }
    }

    fun getDimension(magnitude: Double): Dimension {
        return Dimension().apply {
            this.magnitude = magnitude
            this.unit = "PT"
        }
    }

    fun getRGBColor(rgbValues: String?): OpaqueColor {
        val rgbString = if (rgbValues.isNullOrEmpty()) "255, 255, 255" else rgbValues
        val (r, g, b) = rgbString.split(",")
            .map { it.trim() }
            .map { it.toFloat() / 255 }
        return OpaqueColor().apply {
            rgbColor = RgbColor().apply {
                red = r
                green = g
                blue = b
            }
        }
    }
}
