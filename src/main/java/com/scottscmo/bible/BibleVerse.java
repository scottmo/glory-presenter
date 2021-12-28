package com.scottscmo.bible;

import java.sql.SQLException;
import java.sql.Statement;

public class BibleVerse {
    private static void init(String tableName) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS %s (
                bookIndex TEXT NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(bookIndex, chapter, verse)
            )
        """;
        sql = String.format(sql, tableName);
        Statement stmt = BibleDB.connect().createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
}
