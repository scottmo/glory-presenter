package com.scottscmo.model.song.converters

import com.scottscmo.model.song.Song

object MDConverter {
    private const val SECTION_DELIMITER = "\n---\n"

    fun stringify(song: Song, textGroups: List<String>, maxLines: Int): String {
        val title = "## ${song.title}"

        val sectionTexts = Util.getSectionTexts(song, textGroups, maxLines)
        val sections = sectionTexts.joinToString(SECTION_DELIMITER) { sectionText ->
            textGroups
                .filter { group -> !sectionText[group].isNullOrEmpty() }
                .joinToString("\n") { group ->
                    sectionText[group]?.split("\n")?.joinToString("\n") { "<p>$it</p>" } ?: ""
                }
        }

        return "$title$SECTION_DELIMITER$sections"
    }
}
