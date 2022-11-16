package com.scottmo.services.songs;

import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class SongVerseTable {
    private static final String TABLE_NAME = "verses";

    private final Connection db;

    SongVerseTable(Connection conn) throws SQLException {
        this.db = conn;
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    song_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    text TEXT NOT NULL,
                    locale TEXT NOT NULL,
                    PRIMARY KEY(song_id, name, locale),
                    FOREIGN KEY(song_id) REFERENCES songs(id)
                )""".formatted(TABLE_NAME);
        try (Statement stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    List<SongVerse> getVerses(int songId) throws SQLException {
        String sql = "SELECT name, text, locale FROM %s WHERE song_id = ?".formatted(TABLE_NAME);
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            ResultSet res = stmt.executeQuery();
            List<SongVerse> verses = new ArrayList<>();
            while (res.next()) {
                verses.add(new SongVerse(res.getString("name"), res.getString("text"), res.getString("locale")));
            }
            return verses;
        }
    }

    void insert(int songId, Song song) throws SQLException {
        String sql = "INSERT INTO %s (song_id, name, text, locale) VALUES (?, ?, ?, ?)".formatted(TABLE_NAME);
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            for (SongVerse verse : song.getVerses()) {
                stmt.setInt(1, songId);
                stmt.setString(2, verse.getName());
                stmt.setString(3, verse.getText());
                stmt.setString(4, verse.getLocale());

                stmt.addBatch();
            }
            stmt.executeUpdate();
        }
    }
}
