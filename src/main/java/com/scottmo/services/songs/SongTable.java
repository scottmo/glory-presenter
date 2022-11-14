package com.scottmo.services.songs;

import com.scottmo.data.song.Song;
import javafx.util.Pair;
import org.apache.logging.log4j.util.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class SongTable {
    private static final String TABLE_NAME = "songs";

    private final Connection db;

    SongTable(Connection conn) throws SQLException {
        this.db = conn;
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    authors TEXT,
                    copyright TEXT,
                    publisher TEXT,
                    songbook TEXT,
                    entry TEXT,
                    comments TEXT,
                    verseOrder TEXT
                )""".formatted(TABLE_NAME);
        try (Statement stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    Song getSong(int id) {
        Song song = new Song();

        return song;
    }

    long insert(Song song) throws SQLException {
        List<Pair<String, String>> nonEmptyFields = new ArrayList<>();
        if (!song.getAuthors().isEmpty()) {
            nonEmptyFields.add(new Pair<>("authors", String.join(", ", song.getAuthors())));
        }
        if (Strings.isNotEmpty(song.getCopyright())) {
            nonEmptyFields.add(new Pair<>("copyright", song.getCopyright()));
        }
        if (Strings.isNotEmpty(song.getPublisher())) {
            nonEmptyFields.add(new Pair<>("publisher", song.getPublisher()));
        }
        if (Strings.isNotEmpty(song.getSongBook())) {
            nonEmptyFields.add(new Pair<>("songbook", song.getSongBook()));
        }
        if (Strings.isNotEmpty(song.getEntry())) {
            nonEmptyFields.add(new Pair<>("entry", song.getEntry()));
        }
        if (Strings.isNotEmpty(song.getComments())) {
            nonEmptyFields.add(new Pair<>("comments", song.getComments()));
        }
        if (!song.getVerseOrder().isEmpty()) {
            nonEmptyFields.add(new Pair<>("verseOrder", String.join(" ", song.getVerseOrder())));
        }
        long songId;
        String sql = "INSERT INTO %s (%s) VALUES (%s)".formatted(
                TABLE_NAME,
                String.join(", ", nonEmptyFields.stream().map(Pair::getKey).toList()),
                Strings.repeat("?", nonEmptyFields.size())
        );
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            for (int i = 0; i < nonEmptyFields.size(); i++) {
                var field = nonEmptyFields.get(i);
                stmt.setString(i + 1, field.getValue());
            }
            stmt.executeUpdate();

            ResultSet res = stmt.getGeneratedKeys();
            if (res.next()) {
                songId = res.getLong(1);
            } else {
                throw new RuntimeException("Failed to retrieve id from new song");
            }
        }
        return songId;
    }
}
