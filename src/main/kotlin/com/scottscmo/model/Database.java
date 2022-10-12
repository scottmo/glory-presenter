package com.scottscmo.model;

import com.scottscmo.Config;
import com.scottscmo.Event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final Path dbPath;
    private Connection conn = null;

    public Database(String dbName) throws IOException {
        this.dbPath = Path.of(Config.getRelativePath(dbName + ".db"));
        Event.subscribe(Event.DATA_DIR, eventName -> conn = null);
    }

    public Path getDBPath() {
        return dbPath;
    }

    public Connection connect() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        }
        return conn;
    }

    public boolean isEmpty() {
        return !(Files.exists(dbPath) && dbPath.toFile().length() > 0);
    }
}