package com.scottmo.services.google;

public record TextConfig(
        String wordDelimiter,
        String fontFamily,
        double fontSize,
        String fontColor,
        String fontStyles,
        int numberOfCharactersPerLine,
        int numberOfLinesPerSlide
) {}
