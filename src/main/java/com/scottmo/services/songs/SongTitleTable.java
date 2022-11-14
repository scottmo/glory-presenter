package com.scottmo.services.songs;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

final class SongTitleTable {
    private static final String TABLE_NAME = "titles";

    private final Connection db;

    SongTitleTable(Connection conn) throws SQLException {
        this.db = conn;
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    song_id INTEGER,
                    locale TEXT NOT NULL,
                    text TEXT NOT NULL,
                    PRIMARY KEY(song_id, locale),
                    FOREIGN KEY(song_id) REFERENCES songs(id)
                )""".formatted(TABLE_NAME);
        try (Statement stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
