package com.scottmo.services.songs.store;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;

import javafx.util.Pair;
import org.apache.logging.log4j.util.Strings;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.scottmo.config.AppContext.PRIMARY_LOCALE;

public final class SongStore {
    private static final String DB_NAME = "songs";
    private final SongSchema schema = new SongSchema();
    private final Connection db;

    public SongStore(Path storeLocation) {
        try {
            db = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            try (var stmt = db.createStatement()) {
                stmt.executeUpdate(schema.createSongTable());
                stmt.executeUpdate(schema.createTitleTable());
                stmt.executeUpdate(schema.createVerseTable());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pair<Integer, String>> getTitles(String locale) {
        if (locale == null) locale = PRIMARY_LOCALE;

        List<Pair<Integer, String>> songTitles = new ArrayList<>();

        try (var stmt = db.createStatement()) {
            var res = stmt.executeQuery(new SelectQuery()
                    .addAllTableColumns(schema.titles.table)
                    .addCondition(BinaryCondition.equalTo(schema.titles.locale, locale))
                    .validate().toString());
            while (res.next()) {
                Integer songId = res.getInt(schema.titles.songId.getName());
                String title = res.getString(schema.titles.text.getName());
                songTitles.add(new Pair<>(songId, title));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve song titles from DB!", e);
        }

        return songTitles;
    }

    public Song get(int songId) {
        Song song = new Song(songId);
        try (var stmt = db.createStatement()) {
            ResultSet res = stmt.executeQuery(new SelectQuery()
                    .addAllTableColumns(schema.song.table)
                    .addCondition(BinaryCondition.equalTo(schema.song.id, songId))
                    .validate().toString());
            if (res.next()) {
                String authors = res.getString(schema.song.authors.getName());
                if (Strings.isNotEmpty(authors)) {
                    song.setAuthors(Arrays.stream(authors.split(",")).toList());
                }
                String verseOrder = res.getString(schema.song.verseOrder.getName());
                if (Strings.isNotEmpty(verseOrder)) {
                    song.setVerseOrder(Arrays.stream(verseOrder.split(",")).toList());
                }
                song.setCopyright(res.getString(schema.song.copyright.getName()));
                song.setPublisher(res.getString(schema.song.publisher.getName()));
                song.setSongBook(res.getString(schema.song.songbook.getName()));
                song.setEntry(res.getString(schema.song.entry.getName()));
                song.setComments(res.getString(schema.song.comments.getName()));
            }

            res = stmt.executeQuery(new SelectQuery()
                    .addAllTableColumns(schema.titles.table)
                    .addCondition(BinaryCondition.equalTo(schema.titles.songId, songId))
                    .validate().toString());
            while (res.next()) {
                song.setTitle(
                        res.getString(schema.titles.locale.getName()),
                        res.getString(schema.titles.text.getName()));
            }

            res = stmt.executeQuery(new SelectQuery()
                    .addAllTableColumns(schema.verses.table)
                    .addCondition(BinaryCondition.equalTo(schema.verses.songId, songId))
                    .validate().toString());
            List<SongVerse> verses = new ArrayList<>();
            while (res.next()) {
                verses.add(new SongVerse(
                        res.getString(schema.verses.name.getName()),
                        res.getString(schema.verses.text.getName()),
                        res.getString(schema.verses.locale.getName())));
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
            InsertQuery sql = new InsertQuery(schema.song.table);
            List<String> nonEmptyFields = new ArrayList<>();
            if (!song.getAuthors().isEmpty()) {
                sql.addPreparedColumns(schema.song.authors);
                nonEmptyFields.add(String.join(",", song.getAuthors()));
            }
            if (Strings.isNotEmpty(song.getCopyright())) {
                sql.addPreparedColumns(schema.song.copyright);
                nonEmptyFields.add(song.getCopyright());
            }
            if (Strings.isNotEmpty(song.getPublisher())) {
                sql.addPreparedColumns(schema.song.publisher);
                nonEmptyFields.add(song.getPublisher());
            }
            if (Strings.isNotEmpty(song.getSongBook())) {
                sql.addPreparedColumns(schema.song.songbook);
                nonEmptyFields.add(song.getSongBook());
            }
            if (Strings.isNotEmpty(song.getEntry())) {
                sql.addPreparedColumns(schema.song.entry);
                nonEmptyFields.add(song.getEntry());
            }
            if (Strings.isNotEmpty(song.getComments())) {
                sql.addPreparedColumns(schema.song.comments);
                nonEmptyFields.add(song.getComments());
            }
            if (!song.getVerseOrder().isEmpty()) {
                sql.addPreparedColumns(schema.song.verseOrder);
                nonEmptyFields.add(String.join(",", song.getVerseOrder()));
            }
            sql.validate();
            try (var stmt = db.prepareStatement(sql.toString())) {
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
        var sql = new InsertQuery(schema.titles.table)
                .addPreparedColumns(schema.titles.songId, schema.titles.locale, schema.titles.text);
        sql.validate();
        try (var stmt = db.prepareStatement(sql.toString())) {
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
        var sql = new InsertQuery(schema.verses.table)
                .addPreparedColumns(schema.verses.songId, schema.verses.name, schema.verses.text, schema.verses.locale);
        sql.validate();
        try (var stmt = db.prepareStatement(sql.toString())) {
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
            UpdateQuery sql = new UpdateQuery(schema.song.table)
                .addCondition(BinaryCondition.equalTo(schema.song.id, song.getId()))
                .addSetClause(schema.song.authors, String.join(",", song.getAuthors()))
                .addSetClause(schema.song.copyright, song.getCopyright())
                .addSetClause(schema.song.publisher, song.getPublisher())
                .addSetClause(schema.song.songbook, song.getSongBook())
                .addSetClause(schema.song.entry, song.getEntry())
                .addSetClause(schema.song.comments, song.getComments())
                .addSetClause(schema.song.verseOrder, song.getVerseOrder())
                .validate();
            stmt.executeUpdate(sql.toString());

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
        var deleteSong = new DeleteQuery(schema.song.table)
                .addCondition(BinaryCondition.equalTo(schema.song.id, songId))
                .validate().toString();
        var deleteTitles = new DeleteQuery(schema.titles.table)
                .addCondition(BinaryCondition.equalTo(schema.titles.songId, songId))
                .validate().toString();
        var deleteVerses = new DeleteQuery(schema.verses.table)
                .addCondition(BinaryCondition.equalTo(schema.verses.songId, songId))
                .validate().toString();
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
