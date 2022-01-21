package com.scottscmo.song

data class Song(val title: String, val description: String, val collection: String,
                val lyrics: List<Verse>, val verseOrder: List<String>) {

    fun getVerseText(verseNum: String): Map<String, List<String>>? {
        val verse = this.lyrics.firstOrNull { it.verse == verseNum }
        return verse?.textLines()
    }
}
