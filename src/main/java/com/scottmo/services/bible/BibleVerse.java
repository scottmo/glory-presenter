package com.scottmo.services.bible;

public record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
