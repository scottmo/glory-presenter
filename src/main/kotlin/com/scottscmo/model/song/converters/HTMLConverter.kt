package com.scottscmo.model.song.converters

import com.scottscmo.model.song.Song

object HTMLConverter {

    fun stringify(song: Song, textGroups: List<String>, maxLines: Int): String {
        val sectionTexts = Util.getSectionTexts(song, textGroups, maxLines)
        val sections = sectionTexts.joinToString("\n") { sectionText ->
            val texts = textGroups.joinToString("\n") { group ->
                val text = sectionText[group]?.replace("\n", "<br>")
                "<p class=\"$group\">$text</p>"
            }
            "<section>$texts</section>"
        }

        return "<html>\n<body>\n<div>$sections</div>\n</body>\n</html>"
    }
}
