package com.scottscmo.model.song

class Song() {
    lateinit var title: String
    lateinit var tags: String
    lateinit var sections: List<Section>
    lateinit var order: List<String>

    fun getSectionText(name: String): Map<String, List<String>>? {
        val verse = this.sections.firstOrNull { it.name == name }
        return verse?.textLines()
    }
}
