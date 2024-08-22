package com.scottmo.core.google.impl;

import java.util.Map;

public class SlideConfig {
    private final String alignment;
    private final double indentation;
    private final double x;
    private final double y;
    private final Map<String, FontConfig> fontConfigs;

    public SlideConfig(String alignment, double indentation, double x, double y, Map<String, FontConfig> fontConfigs) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.x = x;
        this.y = y;
        this.fontConfigs = fontConfigs;
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

    public Map<String, FontConfig> getFontConfigs() {
        return fontConfigs;
    }

    public static class FontConfig {
        private final String fontFamily;
        private final double fontSize;
        private final String fontColor;
        private final String fontStyles;
    
        public FontConfig(String fontFamily, double fontSize, String fontColor, String fontStyles) {
            this.fontFamily = fontFamily;
            this.fontSize = fontSize;
            this.fontColor = fontColor;
            this.fontStyles = fontStyles;
        }
    
        public String getFontFamily() {
            return fontFamily;
        }
    
        public double getFontSize() {
            return fontSize;
        }
    
        public String getFontColor() {
            return fontColor;
        }
    
        public String getFontStyles() {
            return fontStyles;
        }
    }
}

