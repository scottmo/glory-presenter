package com.scottscmo.model.song

class Song() {
    lateinit var title: String
    lateinit var tags: String
    lateinit var sections: List<Section>
    lateinit var order: List<String>

    fun getSection(name: String): Section? {
        return this.sections.firstOrNull { it.name == name }
    }
}
