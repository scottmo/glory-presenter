package com.scottmo.services.google;

import java.util.Map;

public class SlideConfig {
    private final String alignment;
    private final double indentation;
    private final double x;
    private final double y;
    private final Map<String, TextConfig> textConfigs;

    public SlideConfig(String alignment, double indentation, double x, double y, Map<String, TextConfig> textConfigs) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.x = x;
        this.y = y;
        this.textConfigs = textConfigs;
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

    public Map<String, TextConfig> getTextConfigs() {
        return textConfigs;
    }

    @Override
    public String toString() {
        return "SlideConfig{" +
                "alignment='" + alignment + '\'' +
                ", indentation=" + indentation +
                ", x=" + x +
                ", y=" + y +
                ", textConfigs=" + textConfigs +
                '}';
    }

    public static class TextConfig {
        private final String fontFamily;
        private final double fontSize;
        private final String fontColor;
        private final String fontStyles;
    
        public TextConfig(String fontFamily, double fontSize, String fontColor, String fontStyles) {
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
    
        @Override
        public String toString() {
            return "TextConfig{" +
                    ", fontFamily='" + fontFamily + '\'' +
                    ", fontSize=" + fontSize +
                    ", fontColor='" + fontColor + '\'' +
                    ", fontStyles='" + fontStyles + '\'' +
                    '}';
        }
    }
}

