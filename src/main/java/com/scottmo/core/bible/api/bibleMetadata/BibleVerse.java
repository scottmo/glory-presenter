package com.scottmo.core.bible.api.bibleMetadata;

public record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
