package com.scottscmo.model.bible;

import java.sql.SQLException;
import java.sql.Statement;

public class BibleVerse {
    public static void init(String tableName) throws SQLException {
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                bookIndex TEXT NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(bookIndex, chapter, verse)
            )
        """, tableName);

        try (Statement stmt = BibleDB.connect().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }


}
