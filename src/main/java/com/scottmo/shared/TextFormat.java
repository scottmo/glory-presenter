package com.scottmo.shared;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;

public class TextFormat {
    private final String family;
    private final double size;
    private final Color color;
    private final boolean isBold;
    private final boolean isItalic;
    private final boolean isUnderlined;
    private final boolean isStrikethrough;
    private final boolean isSmallCaps;

    public TextFormat(String family, double size, String color, boolean isBold, boolean isItalic, 
                boolean isUnderlined, boolean isStrikethrough, boolean isSmallCaps) {
        this.family = family;
        this.size = size;
        this.color = parseColor(color);
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
        this.isStrikethrough = isStrikethrough;
        this.isSmallCaps = isSmallCaps;
    }
    public String getFamily() {
        return family;
    }
    public double getSize() {
        return size;
    }
    public Color getColor() {
        return color;
    }
    public boolean isBold() {
        return isBold;
    }
    public boolean isItalic() {
        return isItalic;
    }
    public boolean isUnderlined() {
        return isUnderlined;
    }
    public boolean isStrikethrough() {
        return isStrikethrough;
    }
    public boolean isSmallCaps() {
        return isSmallCaps;
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