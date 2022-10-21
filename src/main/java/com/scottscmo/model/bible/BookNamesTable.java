package com.scottscmo.model.bible;

import com.scottscmo.bibleMetadata.BibleMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BookNamesTable {
    private static final String DB_NAME = "book_names";

    private void createTable() throws SQLException {
        String sql =  """
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                version VARCHAR(10) NOT NULL,
                PRIMARY KEY (name, version)
            )
        """.formatted(DB_NAME);
        try (Statement stmt = BibleDB.connect().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Retrieve list of book names for each version.
     * structure: [ bookIndex: { bibleVersion: bookName }, ...]
     */
    public List<Map<String, String>> queryAll() throws SQLException {
        List<Map<String, String>> bookNames = new ArrayList<>(Collections.nCopies(BibleMetadata.getNumberOfBooks(), new HashMap<>()));

        String sql = "SELECT * FROM %s".formatted(DB_NAME);
        try (Statement stmt = BibleDB.connect().createStatement()) {
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                bookNames.get(res.getInt("id"))
                        .put(res.getString("version"), res.getString("name"));
            }
        }

        return bookNames;
    }

    public List<String> queryVersions() throws SQLException {
        List<String> versions = new ArrayList<>();
        String sql = "SELECT DISTINCT version FROM %s".formatted(DB_NAME);
        try (Statement stmt = BibleDB.connect().createStatement()) {
            ResultSet res = stmt.executeQuery(sql);
            while (res.next()) {
                versions.add(res.getString("version"));
            }
        }
        return versions;
    }

    public int insert(String version, List<String> bookNames) throws SQLException {
        createTable();

        int inserted;
        String sql = "INSERT INTO %s (id, name, version) VALUES (?, ?, ?)".formatted(DB_NAME);
        try (PreparedStatement stmt = BibleDB.connect().prepareStatement(sql)) {
            for (int i = 0; i < bookNames.size(); i++) {
                stmt.setInt(1, i);
                stmt.setString(2, bookNames.get(i));
                stmt.setString(3, version);
                stmt.addBatch();
            }
            int[] res = stmt.executeBatch();
            inserted = res.length;
            System.out.printf("Inserted %d book names for version %s%n", inserted, version);
        }
        return inserted;
    }
}