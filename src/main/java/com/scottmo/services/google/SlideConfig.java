package com.scottmo.services.google;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlideConfig that = (SlideConfig) o;
        return Objects.equals(unit, that.unit) &&
                Objects.equals(slideWidth, that.slideWidth) &&
                Objects.equals(slideHeight, that.slideHeight) &&
                Objects.equals(paragraph, that.paragraph) &&
                Objects.equals(defaultTextConfig, that.defaultTextConfig) &&
                Objects.equals(defaultAsciiTextConfig, that.defaultAsciiTextConfig) &&
                Objects.equals(defaultNonAsciiTextConfig, that.defaultNonAsciiTextConfig) &&
                Objects.equals(textConfigsOrder, that.textConfigsOrder) &&
                Objects.equals(bibleVersionToTextConfig, that.bibleVersionToTextConfig) &&
                Objects.equals(textConfigs, that.textConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, slideWidth, slideHeight, paragraph, defaultTextConfig,
            defaultAsciiTextConfig, defaultNonAsciiTextConfig, textConfigsOrder,
            bibleVersionToTextConfig, textConfigs);
    }
}

