package com.scottscmo.model.bible;

public record BibleVerseText(
    int bookIndex,
    int chapter,
    int verse,
    String text
) {}
