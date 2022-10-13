package com.scottscmo.model.bible;

import com.scottscmo.bibleMetadata.BibleMetadata;

import java.sql.Connection;
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
        String sql = """
            CREATE TABLE IF NOT EXISTS %s (
                bookIndex INTEGER NOT NULL,
                chapter INTEGER NOT NULL,
                verse INTEGER NOT NULL,
                text TEXT NOT NULL,
                PRIMARY KEY(bookIndex, chapter, verse)
            )
        """.formatted(tableName);
        try (Connection conn = BibleDB.connect()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    private String getTableName(String version) {
        return "bible_" + version;
    }

    public int insert(String version, Map<String, List<List<String>>> bible) throws SQLException {
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
        String tableName = getTableName(version);
        createTable(tableName);

        int inserted;
        String sql = "INSERT INTO %s (bookIndex, chapter, verse, text) VALUES (?, ?, ?, ?)".formatted(tableName);
        try (Connection conn = BibleDB.connect()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (BibleVerse bvt : verses) {
                    stmt.setInt(1, bvt.bookIndex());
                    stmt.setInt(2, bvt.chapter());
                    stmt.setInt(3, bvt.index());
                    stmt.setString(4, bvt.text());
                    stmt.addBatch();
                }
                int[] rs = stmt.executeBatch();
                inserted = rs.length;
            }
        }
        return inserted;
    }

    public List<BibleVerse> query(String version, String bookId, int chapter, List<Integer> verses) throws SQLException {
        List<BibleVerse> bibleVerses = new ArrayList<>();

        String tableName = getTableName(version);
        String sql = "SELECT * FROM %s WHERE bookIndex = ? AND chapter = ?".formatted(tableName);
        if (!verses.isEmpty()) {
            String verseNumberPlaceholder = String.join(",", "?".repeat(verses.size()).split(""));
            sql += " AND verse IN (%s)".formatted(verseNumberPlaceholder);
        }
        try (Connection conn = BibleDB.connect()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, BibleMetadata.getBookIndex(bookId) + ".0"); // .0 because bookIndex was initially a string, TODO: FIX THIS
                stmt.setInt(2, chapter);
                for (int i = 0; i < verses.size(); i++) {
                    stmt.setInt(3 + i, verses.get(i));
                }
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    BibleVerse bvt = new BibleVerse(rs.getInt("bookIndex"), rs.getInt("chapter"),
                            rs.getInt("verse"), rs.getString("text"));
                    bibleVerses.add(bvt);
                }
            }
        }
        return bibleVerses;
    }

    public List<BibleVerse> query(String version, String bookId, int chapter) throws SQLException {
        return query(version, bookId, chapter, Collections.emptyList());
    }
}