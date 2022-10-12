package com.scottscmo.model.bible;

import com.scottscmo.model.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

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

    static void useStatement(Consumer<Statement> run) throws SQLException {
        try (Statement stmt = connect().createStatement()) {
            run.accept(stmt);
        }
    }

    static void usePrepareStatement(String sql, Consumer<PreparedStatement> run) throws SQLException {
        try (PreparedStatement stmt = connect().prepareStatement(sql)) {
            run.accept(stmt);
        }
    }

    static boolean isEmpty() {
        return db.isEmpty();
    }
}