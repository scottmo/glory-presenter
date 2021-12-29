package com.scottscmo.model.bible;

import java.sql.Connection;
import java.sql.SQLException;

import com.scottscmo.model.Database;

class BibleDB {
    private static Database db = new Database("bible");

    static Connection connect() throws SQLException {
        return db.connect();
    }

    static boolean isEmpty() {
        return db.isEmpty();
    }
}
