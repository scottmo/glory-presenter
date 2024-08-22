package com.scottmo.core.bible.impl.store;

public record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
