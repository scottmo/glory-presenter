package com.scottmo.core.songs.impl.store;

import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.shared.Pair;
import com.scottmo.shared.StringUtils;

import org.apache.logging.log4j.util.Strings;

import static com.scottmo.core.songs.impl.store.SongSchema.*;

import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SongStore implements AutoCloseable {
    private final Connection db;

    @Override
    public void close() {
        try {
            db.close();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to close song database", e);
        }
    }

    public SongStore(Path storeLocation) {
        try {
            Files.createDirectories(storeLocation);
            db = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            try (var stmt = db.createStatement()) {
                stmt.executeUpdate(SongTable.createTable());
                stmt.executeUpdate(TitlesTable.createTable());
                stmt.executeUpdate(VersesTable.createTable());
            }
        } catch (SQLException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<Integer, String>> getAllSongDescriptors(List<String> locales) {
        List<Pair<Integer, String>> songDescriptors = new ArrayList<>();
        if (locales == null || locales.isEmpty()) {
            return songDescriptors;
        }

        String delimiter = " - ";
        String sql = String.join(" ",
                "SELECT %s, %s, %s, %s".formatted(SongTable.ID, SongTable.SONGBOOK, SongTable.SONGBOOK_ENTRY, TitlesTable.TEXT),
                "FROM %s".formatted(SongTable.TABLE),
                "INNER JOIN (",
                    "SELECT %s, GROUP_CONCAT(%s, '%s') AS %s".formatted(TitlesTable.SONG_ID, TitlesTable.TEXT, delimiter, TitlesTable.TEXT),
                    "FROM %s".formatted(TitlesTable.TABLE),
                    "WHERE %s IN (%s)".formatted(TitlesTable.LOCALE,
                        String.join(",", java.util.Collections.nCopies(locales.size(), "?"))),
                    "GROUP BY %s".formatted(TitlesTable.SONG_ID),
                ") AS joinedTitles",
                "ON %s.%s = joinedTitles.%s".formatted(SongTable.TABLE, SongTable.ID, TitlesTable.SONG_ID)
                );
        try (var stmt = db.prepareStatement(sql)) {
            for (int i = 0; i < locales.size(); i++) {
                stmt.setString(i + 1, locales.get(i));
            }
            var res = stmt.executeQuery();
            while (res.next()) {
                int songId = res.getInt(SongTable.ID);
                String songbook = res.getString(SongTable.SONGBOOK);
                String entry = res.getString(SongTable.SONGBOOK_ENTRY);
                String title = res.getString(TitlesTable.TEXT);
                String descriptor = Stream.of(songbook, entry, title)
                        .filter(s -> s != null && !s.trim().isEmpty())
                        .collect(Collectors.joining(delimiter));
                songDescriptors.add(new Pair<>(songId, descriptor));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve song descriptors from DB!", e);
        }

        return songDescriptors;
    }

    public Song get(int songId) {
        Song song = new Song(songId);
        try (var stmt = db.createStatement()) {
            ResultSet res = stmt.executeQuery(String.join(" ",
                    "SELECT * FROM %s".formatted(SongTable.TABLE),
                    "WHERE %s = %d".formatted(SongTable.ID, songId)
                    ));
            if (res.next()) {
                String authors = res.getString(SongTable.AUTHORS);
                if (authors != null && !authors.isEmpty()) {
                    song.setAuthors(StringUtils.split(authors));
                }
                String verseOrder = res.getString(SongTable.VERSE_ORDER);
                if (verseOrder != null && !verseOrder.isEmpty()) {
                    song.setVerseOrder(StringUtils.split(verseOrder));
                }
                song.setCopyright(res.getString(SongTable.COPYRIGHT));
                song.setPublisher(res.getString(SongTable.PUBLISHER));
                song.setSongBook(res.getString(SongTable.SONGBOOK));
                song.setEntry(res.getString(SongTable.SONGBOOK_ENTRY));
                song.setComments(res.getString(SongTable.COMMENTS));
            }

            res = stmt.executeQuery(String.join(" ",
                    "SELECT * FROM %s".formatted(TitlesTable.TABLE),
                    "WHERE %s = %d".formatted(TitlesTable.SONG_ID, songId)
                    ));
            while (res.next()) {
                song.setTitle(res.getString(TitlesTable.LOCALE), res.getString(TitlesTable.TEXT));
            }

            res = stmt.executeQuery(String.join(" ",
                    "SELECT * FROM %s".formatted(VersesTable.TABLE),
                    "WHERE %s = %d".formatted(VersesTable.SONG_ID, songId)
                    ));
            List<SongVerse> verses = new ArrayList<>();
            while (res.next()) {
                verses.add(new SongVerse(
                        res.getString(VersesTable.NAME),
                        res.getString(VersesTable.TEXT),
                        res.getString(VersesTable.LOCALE)));
            }
            song.setVerses(verses);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve song from DB!", e);
        }
        return song;
    }

    public boolean exists(int songId) {
        try (var stmt = db.prepareStatement("SELECT 1 FROM " + SongTable.TABLE + " WHERE " + SongTable.ID + "=?")) {
            stmt.setInt(1, songId);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to check song in DB!", e);
        }
    }

    public int store(Song song) {
        if (song.getId() > -1) {
             return update(song);
        }
        return insert(song);
    }

    private int insert(Song song) {
        return inTransaction(() -> {
            List<String> nonEmptyFieldKeys = new ArrayList<>();
            List<String> nonEmptyFields = new ArrayList<>();
            if (!song.getAuthors().isEmpty()) {
                nonEmptyFieldKeys.add(SongTable.AUTHORS);
                nonEmptyFields.add(String.join(",", song.getAuthors()));
            }
            if (Strings.isNotEmpty(song.getCopyright())) {
                nonEmptyFieldKeys.add(SongTable.COPYRIGHT);
                nonEmptyFields.add(song.getCopyright());
            }
            if (Strings.isNotEmpty(song.getPublisher())) {
                nonEmptyFieldKeys.add(SongTable.PUBLISHER);
                nonEmptyFields.add(song.getPublisher());
            }
            if (Strings.isNotEmpty(song.getSongBook())) {
                nonEmptyFieldKeys.add(SongTable.SONGBOOK);
                nonEmptyFields.add(song.getSongBook());
            }
            if (Strings.isNotEmpty(song.getEntry())) {
                nonEmptyFieldKeys.add(SongTable.SONGBOOK_ENTRY);
                nonEmptyFields.add(song.getEntry());
            }
            if (Strings.isNotEmpty(song.getComments())) {
                nonEmptyFieldKeys.add(SongTable.COMMENTS);
                nonEmptyFields.add(song.getComments());
            }
            if (!song.getVerseOrder().isEmpty()) {
                nonEmptyFieldKeys.add(SongTable.VERSE_ORDER);
                nonEmptyFields.add(String.join(",", song.getVerseOrder()));
            }
            String sql = nonEmptyFieldKeys.isEmpty()
                ? "INSERT INTO " + SongTable.TABLE + " DEFAULT VALUES"
                : String.join(" ",
                    "INSERT INTO %s".formatted(SongTable.TABLE),
                    "(%s)".formatted(String.join(",", nonEmptyFieldKeys)),
                    "VALUES(%s)".formatted(String.join(",", java.util.Collections.nCopies(nonEmptyFieldKeys.size(), "?"))));
            int songId;
            try (var stmt = db.prepareStatement(sql)) {
                for (int i = 0; i < nonEmptyFields.size(); i++) {
                    stmt.setString(i + 1, nonEmptyFields.get(i));
                }
                stmt.executeUpdate();

                // return song id
                try (ResultSet res = stmt.getGeneratedKeys()) {
                if (res.next()) {
                    songId = res.getInt(1);
                } else {
                    throw new RuntimeException("Failed to retrieve id from new song");
                }
                }
            }

            insertTitles(songId, song);
            insertVerses(songId, song);
            return songId;
        });
    }

    private void insertTitles(int songId, Song song) throws SQLException {
        String sql = String.join(" ",
                "INSERT INTO %s".formatted(TitlesTable.TABLE),
                "(%s, %s, %s)".formatted(TitlesTable.SONG_ID, TitlesTable.LOCALE, TitlesTable.TEXT),
                "VALUES(?, ?, ?)"
                );
        try (var stmt = db.prepareStatement(sql)) {
            for (String locale : song.getLocales()) {
                stmt.setInt(1, songId);
                stmt.setString(2, locale);
                stmt.setString(3, song.getTitle(locale));

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertVerses(int songId, Song song) throws SQLException {
        String sql = String.join(" ",
        "INSERT INTO %s".formatted(VersesTable.TABLE),
                "(%s, %s, %s, %s)".formatted(VersesTable.SONG_ID, VersesTable.NAME, VersesTable.TEXT, VersesTable.LOCALE),
                "VALUES(?, ?, ?, ?)"
                );
        try (var stmt = db.prepareStatement(sql)) {
            for (SongVerse verse : song.getVerses()) {
                stmt.setInt(1, songId);
                stmt.setString(2, verse.getName());
                stmt.setString(3, verse.getText());
                stmt.setString(4, verse.getLocale());

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private int update(Song song) {
        return inTransaction(() -> {
            deleteRelated(song.getId());
            List<String> keys = List.of(
                SongTable.AUTHORS,
                SongTable.COPYRIGHT,
                SongTable.PUBLISHER,
                SongTable.SONGBOOK,
                SongTable.SONGBOOK_ENTRY,
                SongTable.COMMENTS,
                SongTable.VERSE_ORDER
        );
            List<String> values = List.of(
                String.join(",", song.getAuthors()),
                song.getCopyright(),
                song.getPublisher(),
                song.getSongBook(),
                song.getEntry(),
                song.getComments(),
                String.join(",", song.getVerseOrder())
        );
            String sql = String.join(" ",
                "UPDATE %s SET".formatted(SongTable.TABLE),
                keys.stream().map(s -> s + "=?").collect(Collectors.joining(",")),
                "WHERE %s = %d".formatted(SongTable.ID, song.getId())
        );
            try (var stmt = db.prepareStatement(sql)) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setString(i + 1, values.get(i));
                }
                if (stmt.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Song " + song.getId() + " does not exist");
                }

                insertTitles(song.getId(), song);
                insertVerses(song.getId(), song);
            }
            return song.getId();
        });
    }

    public boolean delete(int songId) {
        return delete(List.of(songId));
    }

    public boolean delete(List<Integer> songIds) {
        return inTransaction(() -> {
            for (Integer songId : songIds) {
                if (songId == null) {
                    throw new IllegalArgumentException("Song id must not be null");
                }
                deleteRelated(songId);
                try (var stmt = db.prepareStatement("DELETE FROM " + SongTable.TABLE + " WHERE " + SongTable.ID + "=?")) {
                    stmt.setInt(1, songId);
                    if (stmt.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Song " + songId + " does not exist");
                    }
                }
            }
            return !songIds.isEmpty();
        });
    }

    private void deleteRelated(int songId) throws SQLException {
        try (var titles = db.prepareStatement("DELETE FROM " + TitlesTable.TABLE + " WHERE " + TitlesTable.SONG_ID + "=?");
             var verses = db.prepareStatement("DELETE FROM " + VersesTable.TABLE + " WHERE " + VersesTable.SONG_ID + "=?")) {
            titles.setInt(1, songId);
            verses.setInt(1, songId);
            titles.executeUpdate();
            verses.executeUpdate();
        }
    }

    private <T> T inTransaction(SqlWork<T> operation) {
        try {
            boolean originalAutoCommit = db.getAutoCommit();
            db.setAutoCommit(false);
            try {
                T result = operation.run();
                db.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    db.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Song database transaction failed", e);
            } finally {
                db.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to perform song database transaction", e);
        }
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run() throws SQLException;
    }
}
