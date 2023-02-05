package com.scottmo.services.songs.store;

import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.util.StringUtils;
import javafx.util.Pair;
import org.apache.logging.log4j.util.Strings;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottmo.services.songs.store.SongSchema.*;

public final class SongStore {
    private final Connection db;

    public SongStore(Path storeLocation) {
        try {
            db = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            try (var stmt = db.createStatement()) {
                stmt.executeUpdate(SongTable.createTable());
                stmt.executeUpdate(TitlesTable.createTable());
                stmt.executeUpdate(VersesTable.createTable());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<Integer, String>> getAllSongDescriptors(List<String> locales) {
        List<Pair<Integer, String>> songDescriptors = new ArrayList<>();

        String delimiter = " - ";
        String sql = String.join(" ",
                "SELECT %s, %s, %s, %s".formatted(SongTable.ID, SongTable.SONGBOOK, SongTable.SONGBOOK_ENTRY, TitlesTable.TEXT),
                "FROM %s".formatted(SongTable.TABLE),
                "INNER JOIN (",
                    "SELECT %s, GROUP_CONCAT(%s, '%s') AS %s".formatted(TitlesTable.SONG_ID, TitlesTable.TEXT, delimiter, TitlesTable.TEXT),
                    "FROM %s".formatted(TitlesTable.TABLE),
                    "WHERE %s".formatted(locales.stream()
                            .map(locale -> "%s == '%s'".formatted(TitlesTable.LOCALE, locale))
                            .collect(Collectors.joining(" OR "))),
                    "GROUP BY %s".formatted(TitlesTable.SONG_ID),
                ") AS joinedTitles",
                "ON %s.%s = joinedTitles.%s".formatted(SongTable.TABLE, SongTable.ID, TitlesTable.SONG_ID)
                );
        try (var stmt = db.createStatement()) {
            var res = stmt.executeQuery(sql);
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
                if (Strings.isNotEmpty(authors)) {
                    song.setAuthors(StringUtils.split(authors));
                }
                String verseOrder = res.getString(SongTable.VERSE_ORDER);
                if (Strings.isNotEmpty(verseOrder)) {
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

    public boolean store(Song song) {
        if (song.getId() > -1) {
             return update(song);
        }
        return insert(song);
    }

    private boolean insert(Song song) {
        int songId;
        try {
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
            String sql = String.join(" ",
                    "INSERT INTO %s".formatted(SongTable.TABLE),
                    "(%s)".formatted(String.join(",", nonEmptyFieldKeys)),
                    "VALUES(%s)".formatted(String.join(",", "?".repeat(nonEmptyFieldKeys.size()).split("")))
                    );
            try (var stmt = db.prepareStatement(sql)) {
                for (int i = 0; i < nonEmptyFields.size(); i++) {
                    stmt.setString(i + 1, nonEmptyFields.get(i));
                }
                stmt.executeUpdate();

                // return song id
                ResultSet res = stmt.getGeneratedKeys();
                if (res.next()) {
                    songId = res.getInt(1);
                } else {
                    throw new RuntimeException("Failed to retrieve id from new song");
                }
            }

            insertTitles(songId, song);
            insertVerses(songId, song);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to insert song to DB!", e);
        }
        return true;
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

    private boolean update(Song song) {
        var locales = song.getLocales();
        if (!locales.isEmpty()) {
            delete(song.getId(), true);
        }
        try (var stmt = db.createStatement()) {
            String sql = String.join(" ",
            "UPDATE %s SET".formatted(SongTable.TABLE),
                    "%s = '%s',".formatted(SongTable.AUTHORS, String.join(",", song.getAuthors())),
                    "%s = '%s',".formatted(SongTable.COPYRIGHT, song.getCopyright()),
                    "%s = '%s',".formatted(SongTable.PUBLISHER, song.getPublisher()),
                    "%s = '%s',".formatted(SongTable.SONGBOOK, song.getSongBook()),
                    "%s = '%s',".formatted(SongTable.SONGBOOK_ENTRY, song.getEntry()),
                    "%s = '%s',".formatted(SongTable.COMMENTS, song.getComments()),
                    "%s = '%s'".formatted(SongTable.VERSE_ORDER, String.join(",", song.getVerseOrder())),
                    "WHERE %s = %d".formatted(SongTable.ID, song.getId())
                    );
            stmt.executeUpdate(sql);

            insertTitles(song.getId(), song);
            insertVerses(song.getId(), song);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to update song!", e);
        }
        return true;
    }

    public boolean delete(int songId) {
        return delete(songId, false);
    }

    private boolean delete(int songId, boolean preserveMetadata) {
        String deleteSql = "DELETE FROM %s WHERE %s = %d";
        String deleteSong = deleteSql.formatted(SongTable.TABLE, SongTable.ID, songId);
        String deleteTitles = deleteSql.formatted(TitlesTable.TABLE, TitlesTable.SONG_ID, songId);
        String deleteVerses = deleteSql.formatted(VersesTable.TABLE, VersesTable.SONG_ID, songId);
        try (var stmt = db.createStatement()) {
            stmt.addBatch(deleteTitles);
            stmt.addBatch(deleteVerses);
            if (!preserveMetadata) {
                stmt.addBatch(deleteSong);
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete song!", e);
        }
        return true;
    }
}
