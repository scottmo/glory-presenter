package com.scottmo.shared;

import com.google.api.services.slides.v1.model.OpaqueColor;
import com.google.api.services.slides.v1.model.RgbColor;
import com.google.common.base.Strings;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TextFormat {
    private final String alignment;
    private final double indentation;
    private final Dimension dimension;
    private final Map<String, Font> font;

    public TextFormat(String alignment, double indentation, Dimension dimension, Map<String, Font> font) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.dimension = dimension;
        this.font = font;
    }

    public String getAlignment() {
        return alignment;
    }
    public double getIndentation() {
        return indentation;
    }
    public Dimension getDimension() {
        return dimension;
    }
    public Map<String, Font> getFont() {
        return font;
    }

    public static class Dimension {
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        public Dimension(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        public double getX() {
            return x;
        }
        public double getY() {
            return y;
        }
        public double getWidth() {
            return width;
        }
        public double getHeight() {
            return height;
        }
    }

    public static class Font {
        private final String family;
        private final double size;
        private final Color color;
        private final String styles;
    
        public Font(String family, double size, String color, String styles) {
            this.family = family;
            this.size = size;
            this.color = parseColor(color);
            this.styles = styles;
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
        public String getStyles() {
            return styles;
        }

        private static Color parseColor(String rgbValues) {
            String rgbString = Strings.isNullOrEmpty(rgbValues) ? "255, 255, 255" : rgbValues;
            List<Float> rgb = Arrays.stream(rgbString.split(","))
                .map(String::trim)
                .map(v -> Float.parseFloat(v)/255)
                .toList();
            return new Color(rgb.get(0), rgb.get(1), rgb.get(2));
        }
    }
}

