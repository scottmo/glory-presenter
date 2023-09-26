package com.scottmo.services.bible.store;

public record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
