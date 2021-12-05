package com.scottscmo.bible;

import java.sql.SQLException;
import java.sql.Statement;

public class BookNames {
    private void createBookNamesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS book_names (
                id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                version VARCHAR(10) NOT NULL,
                PRIMARY KEY (name, version)
            )
        """;
        Statement stmt = BibleDB.connect().createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
}
