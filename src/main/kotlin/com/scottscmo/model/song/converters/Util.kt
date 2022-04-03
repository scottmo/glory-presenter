package com.scottscmo.model.song.converters

import com.scottscmo.model.song.Song

object Util {
    // TODO: refactor this
    private fun getSectionTextsByGroup(song: Song, textGroups: List<String>, maxLines: Int): Map<String, List<String>> {
        if (textGroups.isEmpty()) {
            return emptyMap()
        }
        val data: MutableMap<String, MutableList<String>> = mutableMapOf()
        for (sectionName in song.order) {
            val section = song.getSection(sectionName)
            requireNotNull(section) { "Unable to find section $sectionName" }

            val sectionText = section.textLines()

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

    fun getSectionTexts(song: Song, textGroups: List<String>, maxLines: Int): List<Map<String, String>> {
        val distributedText = getSectionTextsByGroup(song, textGroups, maxLines)
        val numSections = distributedText.values.toList()[0].size
        return (0 until numSections).map { i ->
            textGroups.filter { group -> distributedText.containsKey(group) }
                .associateWith { group -> distributedText[group]!![i] }
        }
    }
}
