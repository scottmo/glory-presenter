package com.scottscmo.model.bible;

import com.scottscmo.model.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

final class BibleDB {
    private static final Database db;

    static {
        try {
            db = new Database("bible");
        } catch (IOException e) {
            throw new RuntimeException("Unable to find bible db", e);
        }
    }

    static Connection connect() throws SQLException {
        return db.connect();
    }

    static boolean isEmpty() {
        return db.isEmpty();
    }
}