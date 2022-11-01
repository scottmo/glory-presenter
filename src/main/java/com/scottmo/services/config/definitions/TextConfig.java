package com.scottmo.services.config.definitions;

public record TextConfig(
        String wordDelimiter,
        String fontFamily,
        double fontSize,
        String fontColor,
        String fontStyles,
        int numberOfCharactersPerLine,
        int numberOfLinesPerSlide
) {}
