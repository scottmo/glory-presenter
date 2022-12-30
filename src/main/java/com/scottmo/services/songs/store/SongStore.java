package com.scottmo.services.songs.store;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;

import com.scottmo.util.LocaleUtil;
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
import java.util.Locale;

import static com.scottmo.config.Constants.PRIMARY_LOCALE;

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
        Song song = new Song();
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
                song.setVerseOrder(res.getString(schema.song.verseOrder.getName()));
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
            if (Strings.isNotEmpty(song.getVerseOrder())) {
                sql.addPreparedColumns(schema.song.verseOrder);
                nonEmptyFields.add(song.getVerseOrder());
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

            sql = new InsertQuery(schema.titles.table)
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

            sql = new InsertQuery(schema.verses.table)
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
        } catch (SQLException e) {
            throw new RuntimeException("Unable to insert song to DB!", e);
        }
        return true;
    }

    private boolean update(Song song) {
        try (var stmt = db.createStatement()) {
            UpdateQuery sql = new UpdateQuery(schema.song.table);
            sql.addCondition(BinaryCondition.equalTo(schema.song.id, song.getId()));
            if (!song.getAuthors().isEmpty()) {
                sql.addSetClause(schema.song.authors, song.getAuthors());
            }
            if (Strings.isNotEmpty(song.getCopyright())) {
                sql.addSetClause(schema.song.copyright, song.getCopyright());
            }
            if (Strings.isNotEmpty(song.getPublisher())) {
                sql.addSetClause(schema.song.publisher, song.getPublisher());
            }
            if (Strings.isNotEmpty(song.getSongBook())) {
                sql.addSetClause(schema.song.songbook, song.getSongBook());
            }
            if (Strings.isNotEmpty(song.getEntry())) {
                sql.addSetClause(schema.song.entry, song.getEntry());
            }
            if (Strings.isNotEmpty(song.getComments())) {
                sql.addSetClause(schema.song.comments, song.getComments());
            }
            if (Strings.isNotEmpty(song.getVerseOrder())) {
                sql.addSetClause(schema.song.verseOrder, song.getVerseOrder());
            }
            sql.validate();
            stmt.addBatch(sql.toString());

            for (String locale : song.getLocales()) {
                sql = new UpdateQuery(schema.titles.table)
                        .addCondition(BinaryCondition.equalTo(schema.titles.songId, song.getId()))
                        .addSetClause(schema.titles.locale, locale)
                        .addSetClause(schema.titles.text, song.getTitle(locale))
                        .validate();
                stmt.addBatch(sql.toString());
            }

            for (SongVerse verse : song.getVerses()) {
                sql = new UpdateQuery(schema.verses.table)
                        .addCondition(BinaryCondition.equalTo(schema.verses.songId, song.getId()))
                        .addSetClause(schema.verses.name, verse.getName())
                        .addSetClause(schema.verses.text, verse.getText())
                        .addSetClause(schema.verses.locale, verse.getLocale())
                        .validate();
                stmt.addBatch(sql.toString());
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to update song!", e);
        }
        return true;
    }

    public boolean delete(int songId) {
        var sql = new DeleteQuery(schema.song.table)
                .addCondition(BinaryCondition.equalTo(schema.song.id, songId))
                .validate().toString();
        try (var stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete song!", e);
        }
        return true;
    }
}
