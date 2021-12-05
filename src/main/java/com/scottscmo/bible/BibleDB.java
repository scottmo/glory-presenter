package com.scottscmo.bible;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.scottscmo.config.Config;

class BibleDB {
    private static Connection conn;

    static {
        Config.subscribe(Config.DIR_DATA, dirData -> {
            conn = null;
        });
    }

    static Connection connect() throws SQLException {
        String dbFilePath = Path.of(Config.get(Config.DIR_DATA), "bible.db").toString();
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        }
        return conn;
    }
}
