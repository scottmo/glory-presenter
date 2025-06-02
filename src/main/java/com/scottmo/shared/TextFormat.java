package com.scottmo.shared;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;

public class TextFormat {
    private String fontFamily;

    private Double fontSize;
    private Color fontColor;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderlined;
    private boolean isStrikethrough;
    private boolean isSmallCaps;

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public void setFontSize(Double fontSize) {
        this.fontSize = fontSize;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = parseColor(fontColor);
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    public boolean isUnderlined() {
        return isUnderlined;
    }

    public void setUnderlined(boolean underlined) {
        isUnderlined = underlined;
    }

    public boolean isStrikethrough() {
        return isStrikethrough;
    }

    public void setStrikethrough(boolean strikethrough) {
        isStrikethrough = strikethrough;
    }

    public boolean isSmallCaps() {
        return isSmallCaps;
    }

    public void setSmallCaps(boolean smallCaps) {
        isSmallCaps = smallCaps;
    }

    private static Color parseColor(String rgbValues) {
        String rgbString = (rgbValues == null || rgbValues.isEmpty()) ? "255, 255, 255" : rgbValues;
        List<Float> rgb = Arrays.stream(rgbString.split(","))
            .map(String::trim)
            .map(v -> Float.parseFloat(v)/255)
            .toList();
        return new Color(rgb.get(0), rgb.get(1), rgb.get(2));
    }
}
