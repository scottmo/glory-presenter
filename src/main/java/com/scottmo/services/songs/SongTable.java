package com.scottmo.services.songs;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

final class SongTable {
    private static final String MAIN_TABLE = "songs";
    private static final String VERSES_TABLE = "verses";
    private static final String TITLES_TABLE = "titles";

    private final Connection db;

    public SongTable(Connection conn) {
        this.db = conn;
    }

    private void createTable() throws SQLException {
        List<String> sqls = List.of("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    authors TEXT,
                    copyright TEXT,
                    publisher TEXT,
                    songbook TEXT,
                    entry TEXT,
                    comments TEXT,
                    verseOrder TEXT
                )""".formatted(MAIN_TABLE),
                """
                CREATE TABLE IF NOT EXISTS %s (
                    song_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    locale TEXT NOT NULL,
                    text TEXT NOT NULL,
                    PRIMARY KEY(song_id, name, locale),
                    FOREIGN KEY(song_id) REFERENCES songs(id)
                )""".formatted(VERSES_TABLE),
                """
                CREATE TABLE IF NOT EXISTS %s (
                    song_id INTEGER,
                    locale TEXT NOT NULL,
                    text TEXT NOT NULL,
                    PRIMARY KEY(song_id, locale),
                    FOREIGN KEY(song_id) REFERENCES songs(id)
                )""".formatted(TITLES_TABLE)
        );
        try (Statement stmt = db.createStatement()) {
            for (String sql : sqls) {
                stmt.executeUpdate(sql);
            }
        }
    }

    public int insert() {
        return 0;
    }
}
