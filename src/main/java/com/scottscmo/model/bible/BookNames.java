package com.scottscmo.model.bible;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookNames {
    private static final String DB_NAME = "book_names";

    public static void init() throws SQLException {
        if (!BibleDB.isEmpty()) return;

        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                version VARCHAR(10) NOT NULL,
                PRIMARY KEY (name, version)
            )
        """, DB_NAME);
        try (Statement stmt = BibleDB.connect().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Retrieve list of book names for each version.
     * structure: [ bookIndex: { bibleVersion: bookName }, ...]
     */
    public static List<Map<String, String>> getBookNames() throws SQLException {
        List<Map<String, String>> bookNames = new ArrayList<>();
        for (int i = 0; i < BibleInfo.getBookInfoMap().size(); i++) {
            bookNames.add(new HashMap<>());
        }

        String sql = String.format("""
            SELECT * FROM %s
        """, DB_NAME);
        try (Statement stmt = BibleDB.connect().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                bookNames.get(rs.getInt("id")).put(rs.getString("version"), rs.getString("name"));
            }
        }

        return bookNames;
    }

    public static int insertBookNames(String version, List<String> bookNames) throws SQLException {
        String sql = String.format("""
            INSERT INTO %s (id, name, version) VALUES (?, ?, ?)
        """, DB_NAME);
        try (PreparedStatement stmt = BibleDB.connect().prepareStatement(sql)) {
            for (int i = 0; i < bookNames.size(); i++) {
                stmt.setInt(1, i);
                stmt.setString(2, bookNames.get(i));
                stmt.setString(3, version);
                stmt.addBatch();
            }
    
            int[] rs = stmt.executeBatch();

            System.out.println(String.format("Inserted %d book names for version %s",
                    rs.length, version));
            return rs.length;
        }
    }
}
