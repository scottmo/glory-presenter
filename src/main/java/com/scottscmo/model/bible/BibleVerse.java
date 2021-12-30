package com.scottscmo.model.bible;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static String getTableName(String version) {
        return "bible_" + version;
    }

    public List<BibleVerseText> getBibleVerse(String version, String bookId, int chapter,
            int[] verses) throws SQLException {
        if (verses == null) verses = new int[]{};

        String sql = String.format("""
            SELECT * FROM %s WHERE bookIndex = ? AND chapter = ?
        """, getTableName(version));
        if (verses.length > 0) {
            String verseNumberPlaceholder = String.join(",", Collections.nCopies(verses.length, "?"));
            sql += " AND verse IN (" + verseNumberPlaceholder + ")";
        }

        try (PreparedStatement stmt = BibleDB.connect().prepareStatement(sql)) {
            stmt.setInt(1, BibleInfo.getBookIndex(bookId));
            stmt.setInt(2, chapter);
            for (int i = 0; i < verses.length; i++) {
                stmt.setInt(3 + i, verses[i]);
            }
            ResultSet rs = stmt.executeQuery();

            List<BibleVerseText> bibleVerses = new ArrayList<>();
            while (rs.next()) {
                BibleVerseText bvt = new BibleVerseText(rs.getInt("bookIndex"),
                        rs.getInt("chapter"), rs.getInt("verse"), rs.getString("text"));
                bibleVerses.add(bvt);
            }
            return bibleVerses;
        }
    }
}
