package com.scottscmo.model.song.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.scottscmo.model.song.Section
import com.scottscmo.model.song.Song

object YAMLConverter {

    private val mapper = ObjectMapper(YAMLFactory().apply {
        configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
        configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
    })

    fun parse(serializedSong: String?): Song? {
        if (serializedSong == null) return null
        return mapper.readValue(serializedSong, Song::class.java)
    }

    fun stringify(song: Song, textGroups: List<String>, maxLines: Int): String {
        song.sections = Util.getSectionTexts(song, textGroups, maxLines).mapIndexed { index, sectionText ->
            Section().apply {
                name = "s$index"
                text = sectionText
            }
        }
        song.order = song.sections.map { it.name }
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
}