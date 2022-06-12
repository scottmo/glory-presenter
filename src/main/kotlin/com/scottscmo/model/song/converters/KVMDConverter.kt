package com.scottscmo.model.song.converters

import com.scottscmo.model.song.Section
import com.scottscmo.model.song.Song
import com.scottscmo.util.KVMD

object KVMDConverter {
    fun parse(kvmdSong: String?): Song? {
        if (kvmdSong == null) return null

        val kvmdObj = KVMD.parse(kvmdSong)
        return Song().apply {
            title = KVMD.getNamespace(kvmdObj)
            tags = KVMD.getMetadata(kvmdObj)["tags"] as String ?: ""
            order = KVMD.getMetadata(kvmdObj)["order"] as List<String>
            sections = KVMD.getContent(kvmdObj).entries
                .sortedBy { it.key }
                .map {
                    Section().apply {
                        name = it.key
                        text = it.value as Map<String, String>
                    }
                }.toList()
        }
    }

    fun stringify(song: Song): String {
        return KVMD.stringify(KVMD.create(
            song.title,
            mapOf(
                "order" to song.order,
                "tags" to song.tags
            ),
            song.sections.associate { it.name to it.text }
        ))
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
        return stringify(song)
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
