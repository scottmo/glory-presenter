package com.scottscmo.model.bible;

record BibleVerse(
    int bookIndex,
    int chapter,
    int index,
    String text
) {}
