package com.scottmo.services.google;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.scottmo.services.google.SlideConfig.TextConfig;

public class SlideConfig {
    private final String unit;
    private final Double slideWidth;
    private final Double slideHeight;
    private final ParagraphConfig paragraph;
    private final String defaultTextConfig;
    private final String defaultAsciiTextConfig;
    private final String defaultNonAsciiTextConfig;
    private final List<String> textConfigsOrder;
    private final Map<String, String> bibleVersionToTextConfig;
    private final Map<String, TextConfig> textConfigs;

    public SlideConfig(String unit, Double slideWidth, Double slideHeight, ParagraphConfig paragraph, String defaultTextConfig, String defaultAsciiTextConfig, String defaultNonAsciiTextConfig, List<String> textConfigsOrder, Map<String, String> bibleVersionToTextConfig, Map<String, TextConfig> textConfigs) {
        this.unit = unit;
        this.slideWidth = slideWidth;
        this.slideHeight = slideHeight;
        this.paragraph = paragraph;
        this.defaultTextConfig = defaultTextConfig;
        this.defaultAsciiTextConfig = defaultAsciiTextConfig;
        this.defaultNonAsciiTextConfig = defaultNonAsciiTextConfig;
        this.textConfigsOrder = textConfigsOrder;
        this.bibleVersionToTextConfig = bibleVersionToTextConfig;
        this.textConfigs = textConfigs;
    }

    public String getUnit() {
        return unit;
    }

    public Double getSlideWidth() {
        return slideWidth;
    }

    public Double getSlideHeight() {
        return slideHeight;
    }

    public ParagraphConfig getParagraph() {
        return paragraph;
    }

    public String getDefaultTextConfig() {
        return defaultTextConfig;
    }

    public String getDefaultAsciiTextConfig() {
        return defaultAsciiTextConfig;
    }

    public String getDefaultNonAsciiTextConfig() {
        return defaultNonAsciiTextConfig;
    }

    public List<String> getTextConfigsOrder() {
        return textConfigsOrder;
    }

    public Map<String, String> getBibleVersionToTextConfig() {
        return bibleVersionToTextConfig;
    }

    public Map<String, TextConfig> getTextConfigs() {
        return textConfigs;
    }

    @Override
    public String toString() {
        return "SlideConfig{" +
                "unit='" + unit + '\'' +
                ", slideWidth=" + slideWidth +
                ", slideHeight=" + slideHeight +
                ", paragraph=" + paragraph +
                ", defaultTextConfig='" + defaultTextConfig + '\'' +
                ", defaultAsciiTextConfig='" + defaultAsciiTextConfig + '\'' +
                ", defaultNonAsciiTextConfig='" + defaultNonAsciiTextConfig + '\'' +
                ", textConfigsOrder=" + textConfigsOrder +
                ", bibleVersionToTextConfig=" + bibleVersionToTextConfig +
                ", textConfigs=" + textConfigs +
                '}';
    }

    public class ParagraphConfig {
        private final String alignment;
        private final double indentation;
        private final double x;
        private final double y;

        public ParagraphConfig(String alignment, double indentation, double x, double y) {
            this.alignment = alignment;
            this.indentation = indentation;
            this.x = x;
            this.y = y;
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

        @Override
        public String toString() {
            return "ParagraphConfig{" +
                    "alignment='" + alignment + '\'' +
                    ", indentation=" + indentation +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public class TextConfig {
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

