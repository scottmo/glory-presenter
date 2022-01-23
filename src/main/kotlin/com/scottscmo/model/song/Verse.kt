package com.scottscmo.model.song

class Verse() {
    lateinit var verse: String
    lateinit var text: Map<String, String>

    fun textLines(): Map<String, List<String>> {
        return this.text.map {
            it.key to it.value.split("\n")
        }.toMap()
    }
}
