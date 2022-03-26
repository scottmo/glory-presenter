package com.scottscmo.model.song

class Song() {
    lateinit var title: String
    lateinit var tags: String
    lateinit var sections: List<Section>
    lateinit var order: List<String>

    fun getSectionTextLines(name: String): Map<String, List<String>>? {
        val section = this.sections.firstOrNull { it.name == name }
        return section?.textLines()
    }

    fun getSectionText(name: String): Map<String, String>? {
        val section = this.sections.firstOrNull { it.name == name }
        return section?.text
    }
}
