package com.scottscmo.model.bible

data class BibleVerse(
    val bookIndex: Int,
    val chapter: Int,
    val verse: Int,
    val text: String
)
