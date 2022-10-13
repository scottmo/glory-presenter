package com.scottscmo.model.bible;

public record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
