package com.scottmo.core.google.api;

import java.util.Map;

public class SlideConfig {
    private final String alignment;
    private final double indentation;
    private final double x;
    private final double y;
    private final Map<String, Font> font;

    public SlideConfig(String alignment, double indentation, double x, double y, Map<String, Font> font) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.x = x;
        this.y = y;
        this.font = font;
    }

    public String getAlignment() {
        return alignment;
    }
    public double getIndentation() {
        return indentation;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public Map<String, Font> getFont() {
        return font;
    }

    public static class Font {
        private final String family;
        private final double size;
        private final String color;
        private final String styles;
    
        public Font(String family, double size, String color, String styles) {
            this.family = family;
            this.size = size;
            this.color = color;
            this.styles = styles;
        }
        public String getFamily() {
            return family;
        }
        public double getSize() {
            return size;
        }
        public String getColor() {
            return color;
        }
        public String getStyles() {
            return styles;
        }
    }
}

