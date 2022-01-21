package com.scottscmo.model.bible

import java.sql.SQLException

internal class BookNamesTable {
    private val DB_NAME = "book_names"

    @Throws(SQLException::class)
    private fun createTable() {
        val sql =  """
            CREATE TABLE IF NOT EXISTS $DB_NAME (
                id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                version VARCHAR(10) NOT NULL,
                PRIMARY KEY (name, version)
            )
        """
        BibleDB.useStatement { stmt ->
            stmt.executeUpdate(sql)
        }
    }

    /**
     * Retrieve list of book names for each version.
     * structure: [ bookIndex: { bibleVersion: bookName }, ...]
     */
    @Throws(SQLException::class)
    fun queryAll(): List<Map<String, String>> {
        val bookNames: List<MutableMap<String, String>> = List(BibleMetadata.bookInfoMap.size) { mutableMapOf() }

        val sql = "SELECT * FROM $DB_NAME"
        BibleDB.useStatement { stmt ->
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                bookNames[rs.getInt("id")][rs.getString("version")] = rs.getString("name")
            }
        }

        return bookNames
    }

    @Throws(SQLException::class)
    fun insert(version: String, bookNames: List<String>): Int {
        createTable()

        var inserted = 0
        val sql = "INSERT INTO $DB_NAME (id, name, version) VALUES (?, ?, ?)"
        BibleDB.usePrepareStatement(sql) { stmt ->
            for (i in bookNames.indices) {
                stmt.setInt(1, i)
                stmt.setString(2, bookNames[i])
                stmt.setString(3, version)
                stmt.addBatch()
            }
            val rs = stmt.executeBatch()
            println("Inserted ${rs.size} book names for version $version")
            inserted = rs.size
        }
        return inserted
    }
}