package com.scottmo.services.google;

import java.util.Objects;

public class TextConfig {
    private final String wordDelimiter;
    private final String fontFamily;
    private final double fontSize;
    private final String fontColor;
    private final String fontStyles;
    private final int numberOfCharactersPerLine;
    private final int numberOfLinesPerSlide;

    public TextConfig(String wordDelimiter, String fontFamily, double fontSize, String fontColor, String fontStyles, int numberOfCharactersPerLine, int numberOfLinesPerSlide) {
        this.wordDelimiter = wordDelimiter;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.fontStyles = fontStyles;
        this.numberOfCharactersPerLine = numberOfCharactersPerLine;
        this.numberOfLinesPerSlide = numberOfLinesPerSlide;
    }

    public String getWordDelimiter() {
        return wordDelimiter;
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

    public int getNumberOfCharactersPerLine() {
        return numberOfCharactersPerLine;
    }

    public int getNumberOfLinesPerSlide() {
        return numberOfLinesPerSlide;
    }

    @Override
    public String toString() {
        return "TextConfig{" +
                "wordDelimiter='" + wordDelimiter + '\'' +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontSize=" + fontSize +
                ", fontColor='" + fontColor + '\'' +
                ", fontStyles='" + fontStyles + '\'' +
                ", numberOfCharactersPerLine=" + numberOfCharactersPerLine +
                ", numberOfLinesPerSlide=" + numberOfLinesPerSlide +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextConfig that = (TextConfig) o;

        if (Double.compare(that.fontSize, fontSize) != 0) return false;
        if (numberOfCharactersPerLine != that.numberOfCharactersPerLine) return false;
        if (numberOfLinesPerSlide != that.numberOfLinesPerSlide) return false;
        if (!wordDelimiter.equals(that.wordDelimiter)) return false;
        if (!fontFamily.equals(that.fontFamily)) return false;
        if (!fontColor.equals(that.fontColor)) return false;
        return fontStyles.equals(that.fontStyles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordDelimiter, fontFamily, fontSize, fontColor, fontStyles,
            numberOfCharactersPerLine, numberOfLinesPerSlide);
    }
}
