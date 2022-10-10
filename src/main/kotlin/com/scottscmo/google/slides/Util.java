package com.scottscmo.google.slides;

import com.google.api.services.slides.v1.model.Dimension;
import com.google.api.services.slides.v1.model.OpaqueColor;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.PageElement;
import com.google.api.services.slides.v1.model.Placeholder;
import com.google.api.services.slides.v1.model.Range;
import com.google.api.services.slides.v1.model.RgbColor;
import com.google.api.services.slides.v1.model.Shape;
import com.google.api.services.slides.v1.model.TextContent;
import com.google.api.services.slides.v1.model.TextElement;
import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Util {
    public static String generateObjectId(String namespace) {
        String uuid = UUID.randomUUID().toString();
        return (namespace + "-" + uuid).substring(0, 38);
    }

    // pageElement?.shape?.text?.textElements
    public static List<TextElement> getTextElements(PageElement pageElement) {
        return Optional.ofNullable(pageElement.getShape())
                .map(Shape::getText)
                .map(TextContent::getTextElements)
                .orElse(Collections.emptyList());
    }

    // slide.pageElements?.find { it.shape?.placeholder?.type == type }
    private static Optional<PageElement> getPlaceholder(Page page, String type) {
        return Optional.ofNullable(page.getPageElements()).orElse(Collections.emptyList())
                .stream().filter(pageElement -> {
                    String placeholderType = Optional.ofNullable(pageElement)
                            .map(PageElement::getShape)
                            .map(Shape::getPlaceholder)
                            .map(Placeholder::getType)
                            .orElse("");
                    return placeholderType.equals(type);
                })
                .findFirst();
    }

    public static Optional<PageElement> getTitlePlaceholder(Page page) {
        return getPlaceholder(page, "TITLE");
    }

    // slide.pageElements?.find { getTextElements(it).isNotEmpty() }
    public static Optional<PageElement> getFirstText(Page page) {
        return Optional.ofNullable(page.getPageElements()).orElse(Collections.emptyList())
                .stream().filter(pageElement -> !getTextElements(pageElement).isEmpty())
                .findFirst();
    }

    public static Range getTextRange(int startIndex) {
        return getTextRange(startIndex, 0);
    }

    public static Range getTextRange(int startIndex, int endIndex) {
        return endIndex != 0
                ? new Range()
                        .setType("FIXED_RANGE")
                        .setStartIndex(startIndex)
                        .setEndIndex(endIndex)
                : new Range()
                        .setType("FROM_START_INDEX")
                        .setStartIndex(startIndex);
    }

    public static Dimension getDimension(Double magnitude) {
        return new Dimension()
                .setMagnitude(magnitude)
                .setUnit("PT");
    }

    public static OpaqueColor getRGBColor(String rgbValues) {
        String rgbString = Strings.isNullOrEmpty(rgbValues) ? "255, 255, 255" : rgbValues;
        List<Float> rgb = Arrays.stream(rgbString.split(","))
                .map(String::trim)
                .map(Float::parseFloat)
                .toList();
        return new OpaqueColor()
                .setRgbColor(new RgbColor()
                        .setRed(rgb.get(0))
                        .setGreen(rgb.get(1))
                        .setBlue(rgb.get(2)));
    }
}
