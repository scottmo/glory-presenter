package com.scottscmo.model.song.adapters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.scottscmo.Config
import com.scottscmo.model.song.Song
import com.scottscmo.model.song.Verse
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object SongYAMLAdapter {
    private val mapper = ObjectMapper(YAMLFactory().apply {
        configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
        configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
    })

    fun deserialize(serializedSong: String?): Song? {
        if (serializedSong == null) return null

        return try {
            mapper.readValue(serializedSong, Song::class.java)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            null
        }
    }

    fun serialize(song: Song, langs: List<String>, maxLines: Int): String {
        val redistributedVerses = serializeToMap(song, langs, maxLines)
        val numVerses = redistributedVerses[langs[0]]?.size ?: 0

        // turn each row of the map into a verse
        val newVerseOrder = mutableListOf<String>()
        val newLyrics = mutableListOf<Verse>()
        for (i in 0 until numVerses) {
            val verseName = "s$i"
            val verseText = mutableMapOf<String, String>()
            for (lang in langs) {
                verseText[lang] = redistributedVerses[lang]!![i]
            }
            newVerseOrder.add(verseName)
            newLyrics.add(Verse().apply {
                verse = verseName
                text = verseText
            })
        }

        song.lyrics = newLyrics
        song.verseOrder = newVerseOrder
        dedupeVerses(song)

        return mapper.writeValueAsString(song)
    }

    private fun dedupeVerses(song: Song) {
        val newVerseOrder = mutableListOf<String>()
        val newLyrics = mutableListOf<Verse>()
        val visitedVerses = mutableMapOf<String, String>()
        for (i in song.lyrics.indices) {
            val verse = song.lyrics[i]
            // use verse text as key and verse number as value
            val verseKey = verse.text.entries.joinToString("&") { "${it.key}=${it.value}" }
            if (visitedVerses.containsKey(verseKey)) {
                // if contains, meaning it's a dupe, reuse the verse number
                newVerseOrder.add(visitedVerses[verseKey]!!)
            } else {
                // unique verses
                newVerseOrder.add(verse.verse)
                newLyrics.add(verse)
                visitedVerses[verseKey] = verse.verse
            }
        }
        song.lyrics = newLyrics
        song.verseOrder = newVerseOrder
    }

    fun getSong(songName: String): Song? {
        return deserialize(getSerializedSong(songName))
    }

    private fun getSerializedSong(songName: String): String? {
        return try {
            val songPath = Path.of(Config.getRelativePath("songs/$songName.yaml"))
            Files.readString(songPath, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            System.err.println(e.message)
            null
        }
    }

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
}