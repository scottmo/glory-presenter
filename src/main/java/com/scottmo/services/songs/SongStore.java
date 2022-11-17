package com.scottmo.services.songs;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import org.apache.logging.log4j.util.Strings;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public Song getSong(int songId) {
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

    public boolean insert(Song song) {
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

            sql = new InsertQuery(schema.titles)
                    .addPreparedColumns(schema.titles.songId, schema.titles.locale, schema.titles.text);
            sql.validate();
            try (var stmt = db.prepareStatement(sql.toString())) {
                for (String locale : song.getTitleLocales()) {
                    stmt.setInt(1, songId);
                    stmt.setString(2, locale);
                    stmt.setString(3, song.getTitle(locale));

                    stmt.addBatch();
                }
                stmt.executeUpdate();
            }

            sql = new InsertQuery(schema.verses)
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
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to insert song to DB!", e);
        }
        return true;
    }
}
