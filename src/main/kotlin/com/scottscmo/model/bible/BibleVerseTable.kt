package com.scottscmo.model.bible

import java.sql.SQLException

internal class BibleVerseTable {
    @Throws(SQLException::class)
    private fun createTable(tableName: String) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $tableName (
                bookIndex INTEGER NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(bookIndex, chapter, verse)
            )
        """
        BibleDB.useStatement { stmt ->
            stmt.executeUpdate(sql)
        }
    }

    fun getTableName(version: String): String {
        return "bible_$version"
    }

    @Throws(SQLException::class)
    fun insert(version: String, bible: Map<String, List<List<String>>>): Int {
        val verses: MutableList<BibleVerse> = ArrayList()
        for (book in bible.keys) {
            val bookIndex = BibleMetadata.getBookIndex(book)
            val chapters = bible[book]!!
            for (c in chapters.indices) {
                val chapter = chapters[c]
                for (v in chapter.indices) {
                    verses.add(BibleVerse(bookIndex, c + 1, v + 1, chapter[v]))
                }
            }
        }
        val tableName = getTableName(version)
        createTable(tableName)

        var inserted = 0
        val sql = "INSERT INTO $tableName (bookIndex, chapter, verse, text) VALUES (?, ?, ?, ?)"
        BibleDB.usePrepareStatement(sql) { stmt ->
            for (bvt in verses) {
                stmt.setInt(1, bvt.bookIndex)
                stmt.setInt(2, bvt.chapter)
                stmt.setInt(3, bvt.verse)
                stmt.setString(4, bvt.text)
                stmt.addBatch()
            }
            val rs = stmt.executeBatch()
            inserted = rs.size
        }
        return inserted
    }

    @Throws(SQLException::class)
    fun query(version: String, bookId: String, chapter: Int, verses: List<Int> = mutableListOf()): List<BibleVerse> {
        val bibleVerses: MutableList<BibleVerse> = ArrayList()

        val tableName = getTableName(version)
        var sql = "SELECT * FROM $tableName WHERE bookIndex = ? AND chapter = ?"
        if (verses.isNotEmpty()) {
            val verseNumberPlaceholder = List(verses.size){ "?" }.joinToString{ "," }
            sql += " AND verse IN ($verseNumberPlaceholder)"
        }
        BibleDB.usePrepareStatement(sql) { stmt ->
            stmt.setString(1, BibleMetadata.getBookIndex(bookId).toString() + ".0") // .0 because bookIndex was initially a string, TODO: FIX THIS
            stmt.setInt(2, chapter)
            for (i in verses.indices) {
                stmt.setInt(3 + i, verses[i])
            }
            val rs = stmt.executeQuery()

            while (rs.next()) {
                val bvt = BibleVerse(rs.getInt("bookIndex"), rs.getInt("chapter"),
                        rs.getInt("verse"), rs.getString("text"))
                bibleVerses.add(bvt)
            }
        }
        return bibleVerses
    }
}