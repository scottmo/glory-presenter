package com.scottscmo.bible;

import java.nio.file.Files;
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

    private static Path getDBPath() {
        return Path.of(Config.get(Config.DIR_DATA), "bible.db");
    }

    static Connection connect() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:" + getDBPath().toString());
        }
        return conn;
    }

    static boolean isEmpty() {
        Path dbPath = getDBPath();
        return !(Files.exists(dbPath) && dbPath.toFile().length() > 0);
    }
}
