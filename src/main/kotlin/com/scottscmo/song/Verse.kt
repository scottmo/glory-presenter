package com.scottscmo.song

data class Verse(val verse: String, val text: Map<String, String>) {
    fun textLines(): Map<String, List<String>> {
        return this.text.map {
            it.key to it.value.split("\n")
        }.toMap()
    }
}
