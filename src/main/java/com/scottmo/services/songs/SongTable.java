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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SongTable {
    private static final String TABLE_NAME = "songs";

    private static final String FIELD_AUTHORS = "authors";
    private static final String FIELD_COPYRIGHT = "copyright";
    private static final String FIELD_PUBLISHER = "publisher";
    private static final String FIELD_SONGBOOK = "songbook";
    private static final String FIELD_ENTRY = "entry";
    private static final String FIELD_COMMENTS = "comments";
    private static final String FIELD_VERSEORDER = "verseOrder";

    private final Connection db;

    SongTable(Connection conn) throws SQLException {
        this.db = conn;
        createTable();
    }

    private void createTable() throws SQLException {
        String textFields = Stream.of(FIELD_AUTHORS, FIELD_COPYRIGHT, FIELD_PUBLISHER,
                FIELD_SONGBOOK, FIELD_ENTRY, FIELD_COMMENTS, FIELD_VERSEORDER)
                .map(s -> s + " TEXT")
                .collect(Collectors.joining(","));
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    %s
                )""".formatted(TABLE_NAME, textFields);
        try (Statement stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    Song getSong(long id) throws SQLException {
        Song song = new Song();

        String sql = "SELECT * FROM %s WHERE id = ?".formatted(TABLE_NAME);
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                String authors = res.getString(FIELD_AUTHORS);
                if (Strings.isNotEmpty(authors)) {
                    Arrays.stream(authors.split(",")).forEach(song::addAuthor);
                }
                String verseOrder = res.getString(FIELD_VERSEORDER);
                if (Strings.isNotEmpty(verseOrder)) {
                    song.setVerseOrder(Arrays.stream(verseOrder.split(",")).toList());
                }
                song.setCopyright(res.getString(FIELD_COPYRIGHT));
                song.setPublisher(res.getString(FIELD_PUBLISHER));
                song.setSongBook(res.getString(FIELD_SONGBOOK));
                song.setEntry(res.getString(FIELD_ENTRY));
                song.setComments(res.getString(FIELD_COMMENTS));
            }
        }

        return song;
    }

    long insert(Song song) throws SQLException {
        List<Pair<String, String>> nonEmptyFields = new ArrayList<>();
        if (!song.getAuthors().isEmpty()) {
            nonEmptyFields.add(new Pair<>(FIELD_AUTHORS, String.join(",", song.getAuthors())));
        }
        if (Strings.isNotEmpty(song.getCopyright())) {
            nonEmptyFields.add(new Pair<>(FIELD_COPYRIGHT, song.getCopyright()));
        }
        if (Strings.isNotEmpty(song.getPublisher())) {
            nonEmptyFields.add(new Pair<>(FIELD_PUBLISHER, song.getPublisher()));
        }
        if (Strings.isNotEmpty(song.getSongBook())) {
            nonEmptyFields.add(new Pair<>(FIELD_SONGBOOK, song.getSongBook()));
        }
        if (Strings.isNotEmpty(song.getEntry())) {
            nonEmptyFields.add(new Pair<>(FIELD_ENTRY, song.getEntry()));
        }
        if (Strings.isNotEmpty(song.getComments())) {
            nonEmptyFields.add(new Pair<>(FIELD_COMMENTS, song.getComments()));
        }
        if (!song.getVerseOrder().isEmpty()) {
            nonEmptyFields.add(new Pair<>(FIELD_VERSEORDER, String.join(" ", song.getVerseOrder())));
        }
        String sql = "INSERT INTO %s (%s) VALUES (%s)".formatted(
                TABLE_NAME,
                String.join(", ", nonEmptyFields.stream().map(Pair::getKey).toList()),
                Strings.repeat("?", nonEmptyFields.size())
        );
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            for (int i = 0; i < nonEmptyFields.size(); i++) {
                Pair<String, String> field = nonEmptyFields.get(i);
                stmt.setString(i + 1, field.getValue());
            }
            stmt.executeUpdate();

            // return song id
            ResultSet res = stmt.getGeneratedKeys();
            if (res.next()) {
                return res.getLong(1);
            } else {
                throw new RuntimeException("Failed to retrieve id from new song");
            }
        }
    }
}
