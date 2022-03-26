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

    fun serialize(song: Song, textGroups: List<String>, maxLines: Int): String {
        val redistributedSections = serializeToMap(song, textGroups, maxLines)
        val numSections = redistributedSections.values.toList()[0].size

        // turn each row of the map into a section
        val newOrder = mutableListOf<String>()
        val newSections = mutableListOf<Section>()
        for (i in 0 until numSections) {
            val sectionName = "s$i"
            val sectionText = mutableMapOf<String, String>()
            for (group in textGroups) {
                redistributedSections[group]?.let { redistributedSectionText ->
                    sectionText[group] = redistributedSectionText[i]
                }
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

    private fun serializeToMap(song: Song, textGroups: List<String>, maxLines: Int): Map<String, List<String>> {
        if (textGroups.isEmpty()) {
            return emptyMap()
        }
        val data: MutableMap<String, MutableList<String>> = mutableMapOf()
        for (sectionName in song.order) {
            val sectionText = song.getSectionTextLines(sectionName)

            require(!sectionText.isNullOrEmpty())

            // assuming all langs have same # of section lines
            val numSectionLines = sectionText.values.toList()[0].size
            var numSlidePerThisSection = 0
            while (numSlidePerThisSection * maxLines < numSectionLines) {
                for (group in textGroups) {
                    val lines = data[group] ?: mutableListOf()
                    var line = ""
                    sectionText[group]?.let { sectionLines ->
                        for (i in 0 until maxLines) {
                            val currLineInSection = numSlidePerThisSection * maxLines + i
                            if (currLineInSection < sectionLines.size) {
                                line += sectionLines[currLineInSection] + "\n"
                            }
                        }
                        lines.add(line.trim())
                        data[group] = lines
                    }
                }
                numSlidePerThisSection++
            }
        }
        return data
    }
}