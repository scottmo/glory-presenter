package com.scottscmo.model.bible

import java.sql.SQLException

class BibleModel {
    companion object {
        val instance: BibleModel = BibleModel()
    }

    private val bibleVerseTable: BibleVerseTable = BibleVerseTable()
    private val bookNamesTable: BookNamesTable = BookNamesTable()
    private var bookNames: List<Map<String, String>>? = null // cache

    fun insert(bible: Map<String, List<List<String>>>, version: String): Boolean {
        try {
            val insertedCount = bookNamesTable.insert(version, ArrayList(bible.keys))
            println("Inserted $insertedCount book names.")
        } catch (e: SQLException) {
            System.err.println("Failed to insert book names!")
            e.printStackTrace()
            return false
        }
        try {
            val insertedCount = bibleVerseTable.insert(version, bible)
            println("Inserted $insertedCount bible verses.")
        } catch (e: SQLException) {
            System.err.println("Failed to insert bible verses!")
            e.printStackTrace()
            return false
        }
        bookNames = null
        return true
    }

    fun getBibleVerses(ref: BibleReference): Map<String, List<BibleVerse>>? {
        val versions = ref.versions
        val bookId = ref.book
        return try {
            val bibleVerses: MutableMap<String, List<BibleVerse>> = HashMap()
            for (version in versions) {
                val verses: MutableList<BibleVerse> = ArrayList()
                for (range in ref.ranges) {
                    verses.addAll(bibleVerseTable.query(version, bookId, range.chapter, range.verses))
                }
                bibleVerses[version] = verses
            }
            bibleVerses
        } catch (e: SQLException) {
            System.err.println("Failed to get bible verses!")
            e.printStackTrace()
            null
        }
    }

    /**
     * @return { $bibleVersion: $bookName }
     */
    fun getBookNames(bookId: String): Map<String, String>? {
        if (this.bookNames == null) {
            try {
                this.bookNames = this.bookNamesTable.queryAll()
            } catch (e: SQLException) {
                System.err.println("Failed to get book names!")
                e.printStackTrace()
            }
        }
        return this.bookNames?.getOrNull(BibleMetadata.getBookIndex(bookId))
    }
}