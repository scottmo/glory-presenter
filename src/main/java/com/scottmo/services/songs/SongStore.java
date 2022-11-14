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

    public boolean insert(Song song) {
        try {
            long songId = songTable.insert(song);
        } catch (SQLException e) {

        }

        return false;
    }
}
