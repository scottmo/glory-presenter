package com.scottscmo.song.adapters

import com.scottscmo.song.Song
import java.util.stream.Collectors

object SongSlideTextAdapter {
    private const val SEPARATOR = "\n\n---\n\n"

    fun serializeToList(song: Song?, langs: List<String>?, maxLines: Int): List<String> {
        if (song == null || langs == null || langs.isEmpty()) {
            return emptyList()
        }
        val slides: MutableList<String> = ArrayList()
        for (verseNumber in song.verseOrder) {
            val verseText = song.getVerseText(verseNumber)
            assert(verseText != null && langs.all { verseText.containsKey(it) })

            // assuming all langs have same # of verse lines
            val numVerseLines = verseText!![langs[0]]!!.size
            var numSlidePerThisVerse = 0
            while (numSlidePerThisVerse * maxLines < numVerseLines) {
                var slide = ""
                for (lang in langs) {
                    val verseLines = verseText[lang] ?: continue
                    for (i in 0 until maxLines) {
                        val currLineInVerse = numSlidePerThisVerse * maxLines + i
                        if (currLineInVerse < verseLines.size) {
                            slide += verseLines[currLineInVerse] + "\n"
                        }
                    }
                }
                slides.add(slide.trim())
                numSlidePerThisVerse++
            }
        }
        return slides
    }

    fun serialize(song: Song?, langs: List<String>?, maxLines: Int): String {
        return serializeToList(song, langs, maxLines).joinToString { SEPARATOR }
    }
}