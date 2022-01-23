package com.scottscmo.song

class Song() {
    lateinit var title: String
    lateinit var description: String
    lateinit var collection: String
    lateinit var lyrics: List<Verse>
    lateinit var verseOrder: List<String>

    fun getVerseText(verseNum: String): Map<String, List<String>>? {
        val verse = this.lyrics.firstOrNull { it.verse == verseNum }
        return verse?.textLines()
    }
}
