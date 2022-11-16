package com.scottmo.services.songs;

import com.scottmo.data.song.Song;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SongStore {
    private static final String DB_NAME = "songs";
    private final SongTable songTable;
    private final SongTitleTable songTitleTable;
    private final SongVerseTable songVerseTable;

    public SongStore(Path storeLocation) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            songTable = new SongTable(conn);
            songTitleTable = new SongTitleTable(conn);
            songVerseTable = new SongVerseTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Song getSong(int songId) {
        try {
            Song song = songTable.getSong(songId);
            var titles = songTitleTable.getTitles(songId);
            titles.entrySet().stream().forEach(entry -> {
                song.setTitle(entry.getKey(), entry.getValue());
            });
            var verses = songVerseTable.getVerses(songId);
            song.setVerses(verses);
            return song;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve song from DB!", e);
        }
    }

    public boolean insert(Song song) {
        try {
            int songId = songTable.insert(song);
            songTitleTable.insert(songId, song);
            songVerseTable.insert(songId, song);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to insert song to DB!", e);
        }
    }
}
