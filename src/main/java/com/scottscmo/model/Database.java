package com.scottscmo.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.scottscmo.config.Config;

public class Database {
    private String dbName;
    private Connection conn;

    public Database(String dbName) {
        this.dbName = dbName;

        Config.subscribe(Config.DIR_DATA, dirData -> {
            conn = null;
        });
    }

    private Path getDBPath() {
        return Path.of(Config.get(Config.DIR_DATA), this.dbName + ".db");
    }

    public Connection connect() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:" + getDBPath().toString());
        }
        return conn;
    }

    public boolean isEmpty() {
        Path dbPath = getDBPath();
        return !(Files.exists(dbPath) && dbPath.toFile().length() > 0);
    }
}
