package com.scottscmo.model.bible

import java.util.regex.Pattern

/**
 * e.g. cuv,niv - john 1:2-3;3:4;5:1-3
 */
class BibleReference(bibleReferenceStr: String) {
    val versions: List<String>
    val book: String
    val ranges: List<VerseRange>

    init {
        require(bibleReferenceStr.isNotEmpty()) { "BibleReference is missing completely!" }
        var refString = bibleReferenceStr

        var versions: List<String>? = null
        val bibleReferenceStrParts = refString.split(" - ")
        if (bibleReferenceStrParts.size > 1) {
            versions = bibleReferenceStrParts[0].split(",")
            refString = bibleReferenceStrParts[1]
        }

        require(!versions.isNullOrEmpty()) {
            "BibleReference '$refString' is missing bible versions"
        }
        this.versions = versions

        var book: String? = null
        var ranges: List<VerseRange>? = null
        val matcher = RE_VERSE_NOTATION.matcher(refString)
        if (matcher.find() && matcher.groupCount() == 2) {
            book = matcher.group(1).trim().replace("\\s+".toRegex(), " ").lowercase()

            val rangesStr = matcher.group(2).trim().replace("\\s+".toRegex(), "")
            ranges = rangesStr.split(";")
                    .filter(String::isNotEmpty)
                    .map(VerseRange::of)
        }
        require(!book.isNullOrEmpty() && !ranges.isNullOrEmpty()) {
            "BibleReference '$refString' is missing book name or verse ranges!"
        }

        this.book = book
        this.ranges = ranges
    }

    override fun toString(): String {
        return if (this.ranges.isEmpty()) {
            this.book
        } else {
            this.book + " " + this.ranges.joinToString(";")
        }
    }

    data class VerseRange(val chapter: Int, val verses: List<Int>) {
        companion object {
            fun of(verseRangeString: String): VerseRange {
                val rangeSplits = verseRangeString.split(":")
                val chapter = rangeSplits[0].toInt()
                val verses = if (rangeSplits.size > 1) parseVerseReferences(rangeSplits[1]) else emptyList()
                return VerseRange(chapter, verses)
            }

            private fun parseVerseReferences(verseRanges: String): List<Int> {
                val verses: MutableList<Int> = ArrayList()
                for (verse in verseRanges.split(",")) {
                    if (verse.isEmpty()) continue
                    if (verse.contains("-")) { // verse range
                        val verseSplits = verse.split("-")
                        val minVerse = verseSplits[0].toInt()
                        val maxVerse = verseSplits[1].toInt()
                        require(minVerse <= maxVerse) {
                            "Invalid verse range: $verse. End verse should not be less than start verse."
                        }
                        for (i in minVerse..maxVerse) {
                            verses.add(i)
                        }
                    } else { // single verse
                        verses.add(verse.toInt())
                    }
                }
                return verses
            }
        }

        override fun toString(): String {
            if (this.chapter == 0) {
                return ""
            }
            if (this.verses.isEmpty()) {
                return this.chapter.toString()
            }
            val verseRangeStrs: MutableList<String> = ArrayList()
            var startVerse: Int = this.verses[0]
            var endVerse = -1
            for (i in 1 until this.verses.size) {
                if (this.verses[i - 1] + 1 == this.verses[i]) { // consecutive, update endVerse to cover current verse
                    endVerse = this.verses[i]
                } else { // not consecutive
                    verseRangeStrs.add(formatVerseReferences(startVerse, endVerse))
                    startVerse = this.verses[i]
                    endVerse = -1
                }
            }
            verseRangeStrs.add(formatVerseReferences(startVerse, endVerse))
            return this.chapter.toString() + ":" + verseRangeStrs.joinToString(",")
        }

        private fun formatVerseReferences(startVerse: Int, endVerse: Int): String {
            return if (endVerse <= startVerse) startVerse.toString() + "" else "$startVerse-$endVerse"
        }
    }

    companion object {
        private val RE_VERSE_NOTATION = Pattern.compile("([\\d]?[A-z\\s]+)\\s+([\\d,;:\\-\\s]+)")
    }
}