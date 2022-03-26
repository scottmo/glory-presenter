package com.scottscmo.model.song.adapters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.scottscmo.Config
import com.scottscmo.model.song.Song
import com.scottscmo.model.song.Section
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
        return mapper.readValue(serializedSong, Song::class.java)
    }

    fun serialize(song: Song, langs: List<String>, maxLines: Int): String {
        val redistributedSections = serializeToMap(song, langs, maxLines)
        val numSections = redistributedSections[langs[0]]?.size ?: 0

        // turn each row of the map into a section
        val newOrder = mutableListOf<String>()
        val newSections = mutableListOf<Section>()
        for (i in 0 until numSections) {
            val sectionName = "s$i"
            val sectionText = mutableMapOf<String, String>()
            for (lang in langs) {
                sectionText[lang] = redistributedSections[lang]!![i]
            }
            newOrder.add(sectionName)
            newSections.add(Section().apply {
                name = sectionName
                text = sectionText
            })
        }

        song.sections = newSections
        song.order = newOrder
        dedupeSections(song)

        return mapper.writeValueAsString(song)
    }

    private fun dedupeSections(song: Song) {
        val newOrder = mutableListOf<String>()
        val newSections = mutableListOf<Section>()
        val visitedSections = mutableMapOf<String, String>()
        for (i in song.sections.indices) {
            val section = song.sections[i]
            // use section text as key and section number as value
            val sectionKey = section.text.entries.joinToString("&") { "${it.key}=${it.value}" }
            if (visitedSections.containsKey(sectionKey)) {
                // if contains, meaning it's a dupe, reuse the section number
                newOrder.add(visitedSections[sectionKey]!!)
            } else {
                // unique sections
                newOrder.add(section.name)
                newSections.add(section)
                visitedSections[sectionKey] = section.name
            }
        }
        song.sections = newSections
        song.order = newOrder
    }

    fun getSong(songName: String): Song? {
        return deserialize(getSerializedSong(songName))
    }

    private fun getSerializedSong(songName: String): String? {
        val songPath = Path.of(Config.getRelativePath("${Config.SONG_YAML_DIR}/$songName.yaml"))
        return Files.readString(songPath, StandardCharsets.UTF_8)
    }

    private fun serializeToMap(song: Song, langs: List<String>, maxLines: Int): Map<String, List<String>> {
        if (langs.isEmpty()) {
            return emptyMap()
        }
        val data: MutableMap<String, MutableList<String>> = mutableMapOf()
        for (sectionName in song.order) {
            val sectionText = song.getSectionText(sectionName)

            require(!sectionText.isNullOrEmpty() && langs.all { sectionText.containsKey(it) })

            // assuming all langs have same # of section lines
            val numSectionLines = sectionText[langs[0]]!!.size
            var numSlidePerThisSection = 0
            while (numSlidePerThisSection * maxLines < numSectionLines) {
                for (lang in langs) {
                    val lines = data[lang] ?: mutableListOf()
                    var line = ""
                    sectionText[lang]?.let { sectionLines ->
                        for (i in 0 until maxLines) {
                            val currLineInSection = numSlidePerThisSection * maxLines + i
                            if (currLineInSection < sectionLines.size) {
                                line += sectionLines[currLineInSection] + "\n"
                            }
                        }
                        lines.add(line.trim())
                        data[lang] = lines
                    }
                }
                numSlidePerThisSection++
            }
        }
        return data
    }
}