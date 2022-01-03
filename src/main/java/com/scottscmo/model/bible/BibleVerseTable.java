package com.scottscmo.model.bible;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class BibleVerseTable {
    private void createTable(String tableName) throws SQLException {
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                bookIndex INTEGER NOT NULL,
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

    String getTableName(String version) {
        return "bible_" + version;
    }

    int insertBibleVerses(String version, Map<String, List<List<String>>> bible)
            throws SQLException {

        List<BibleVerse> verses = new ArrayList<>();
        for (String book : bible.keySet()) {
            int bookIndex = BibleMetadata.getBookIndex(book);
            List<List<String>> chapters = bible.get(book);
            for (int c = 0; c < chapters.size(); c++) {
                List<String> chapter = chapters.get(c);
                for (int v = 0; v < chapter.size(); v++) {
                    verses.add(new BibleVerse(bookIndex, c + 1, v + 1, chapter.get(v)));
                }
            }
        }

        createTable(version);
        String sql = String.format("""
            INSERT INTO %s (bookIndex, chapter, verse, text) VALUES (?, ?, ?, ?)
        """, getTableName(version));
        try (PreparedStatement stmt = BibleDB.connect().prepareStatement(sql)) {
            for (BibleVerse bvt : verses) {
                stmt.setInt(1, bvt.bookIndex());
                stmt.setInt(2, bvt.chapter());
                stmt.setInt(3, bvt.verse());
                stmt.setString(4, bvt.text());
                stmt.addBatch();
            }
            stmt.executeBatch();

            return verses.size();
        }
    }

    List<BibleVerse> getBibleVerses(String version, String bookId, int chapter,
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
            stmt.setString(1, BibleMetadata.getBookIndex(bookId) + ".0"); // .0 because bookIndex was initially a string, TODO: FIX THIS
            stmt.setInt(2, chapter);
            for (int i = 0; i < verses.length; i++) {
                stmt.setInt(3 + i, verses[i]);
            }
            ResultSet rs = stmt.executeQuery();

            List<BibleVerse> bibleVerses = new ArrayList<>();
            while (rs.next()) {
                BibleVerse bvt = new BibleVerse(rs.getInt("bookIndex"),
                        rs.getInt("chapter"), rs.getInt("verse"), rs.getString("text"));
                bibleVerses.add(bvt);
            }
            return bibleVerses;
        }
    }
}
