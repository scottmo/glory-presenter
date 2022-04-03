package com.scottscmo.model.song.converters

import com.scottscmo.model.song.Song

object HTMLConverter {

    fun stringify(song: Song, textGroups: List<String>, maxLines: Int): String {

        val title = "<section><h2>${song.title}</h2></section>\n"

        val sectionTexts = Util.getSectionTexts(song, textGroups, maxLines)
        val sections = sectionTexts.joinToString("\n") { sectionText ->
            val texts = textGroups
                .filter { group -> sectionText[group] != null }
                .joinToString("\n") { group ->
                    val text = sectionText[group]?.replace("\n", "<br>")
                    "<p class=\"$group\">$text</p>"
                }
            "<section>$texts</section>"
        }

        return """
            <html>
                <body>
                    <div>
                        $title
                        $sections
                    </div>
                </body>
            </html>
        """.trimIndent()
    }
}
