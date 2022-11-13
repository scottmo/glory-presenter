package com.scottmo.services.songs;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SongStore {
    private static final String DB_NAME = "songs";
    private final SongTable songTable;

    public SongStore(Path storeLocation) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            songTable = new SongTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
