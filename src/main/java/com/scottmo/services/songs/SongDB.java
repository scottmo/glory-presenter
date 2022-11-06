package com.scottmo.services.songs;

import com.scottmo.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SongDB {
    private static final String DB_NAME = "songs";
    private static Connection conn;

    static Connection get() throws SQLException {
        if (conn == null) {
            Database db;
            try {
                db = new Database(DB_NAME);
            } catch (IOException e) {
                throw new RuntimeException("Unable to find %s.db".formatted(DB_NAME), e);
            }
            conn = db.connect();
        }
        return conn;
    }
}
