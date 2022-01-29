package com.scottscmo.model.song.adapters

import com.scottscmo.model.song.Song
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.jvm.Throws

object SongCSVAdapter {
    private fun serializeToMap(song: Song?, langs: List<String>?, maxLines: Int): Map<String, List<String>> {
        if (song == null || langs.isNullOrEmpty()) {
            return emptyMap()
        }
        val data: MutableMap<String, MutableList<String>> = mutableMapOf()
        for (verseNumber in song.verseOrder) {
            val verseText = song.getVerseText(verseNumber)

            require(!verseText.isNullOrEmpty() && langs.all { verseText.containsKey(it) })

            // assuming all langs have same # of verse lines
            val numVerseLines = verseText[langs[0]]!!.size
            var numSlidePerThisVerse = 0
            while (numSlidePerThisVerse * maxLines < numVerseLines) {
                for (lang in langs) {
                    val lines = data[lang] ?: mutableListOf()
                    var line = ""
                    val verseLines = verseText[lang]
                    if (verseLines.isNullOrEmpty()) {
                        continue
                    }
                    for (i in 0 until maxLines) {
                        val currLineInVerse = numSlidePerThisVerse * maxLines + i
                        if (currLineInVerse < verseLines.size) {
                            line += verseLines[currLineInVerse] + "\n"
                        }
                    }
                    lines.add(line.trim())
                    data[lang] = lines
                }
                numSlidePerThisVerse++
            }
        }
        return data
    }

    @Throws(IOException::class)
    fun serializeToCSV(filePath: String, song: Song, langs: List<String>, maxLines: Int): Unit {
        val headers = langs.map { lang -> "title_$lang" }.toTypedArray()
        val data = serializeToMap(song, langs, maxLines)
        val numRows = data[langs[0]]?.size ?: 0

        CSVPrinter(FileWriter(filePath, StandardCharsets.UTF_8), CSVFormat.DEFAULT.withHeader(*headers)).use {
            for (i in 0 until numRows) {
                val row = langs.map { lang -> data[lang]?.get(i) }
                it.printRecord(row)
            }
        }
    }
}