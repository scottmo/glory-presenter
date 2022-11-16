package com.scottmo.services.songs;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ConstraintClause;
import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.scottmo.data.song.Song;
import javafx.scene.layout.ColumnConstraints;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SongTitleTable {
    private static final DbTable table = (new DbSpec()).getDefaultSchema().addTable("titles");
    private static final DbColumn cSongId = table.addColumn("song_id", "TEXT", null);
    private static final DbColumn cText = table.addColumn("text", "TEXT", null);
    private static final DbColumn cLocale = table.addColumn("locale", "TEXT", null);
    private static final String TABLE_NAME = "titles";
    private static final String FIELD_SONG_ID = "song_id";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_LOCALE = "locale";

    private final Connection db;

    SongTitleTable(Connection conn) throws SQLException {
        this.db = conn;
        createTable();
    }

    private void createTable() throws SQLException {
        table.primaryKey(null, cSongId.getName(), cLocale.getName());

        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    song_id INTEGER,
                    locale TEXT NOT NULL,
                    text TEXT NOT NULL,
                    PRIMARY KEY(song_id, locale),
                    FOREIGN KEY(song_id) REFERENCES songs(id)
                )""".formatted(TABLE_NAME);
        try (Statement stmt = db.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    Map<String, String> getTitles(int songId) throws SQLException {
//        String sql = new SelectQuery()
//                .addColumns(FIELD_TEXT, FIELD_LOCALE)
//
//                .addCondition(BinaryCondition.equalTo(FIELD_SONG_ID, songId))
        String sql = "SELECT locale, text FROM %s WHERE song_id = ?".formatted(TABLE_NAME);
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            ResultSet res = stmt.executeQuery();
            Map<String, String> titles = new HashMap<>();
            while (res.next()) {
                titles.put(res.getString("locale"), res.getString("text"));
            }
            return titles;
        }
    }

    List<Pair<Integer, String>> getAllDefaultTitles() throws SQLException {
        List<Pair<Integer, String>> songTitles = new ArrayList<>();
        return songTitles;
    }

    void insert(int songId, Song song) throws SQLException {
        String sql = "INSERT INTO %s (song_id, locale, text) VALUES (?, ?, ?)".formatted(TABLE_NAME);
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            for (String locale : song.getTitleLocales()) {
                stmt.setInt(1, songId);
                stmt.setString(2, locale);
                stmt.setString(3, song.getTitle(locale));

                stmt.addBatch();
            }
            stmt.executeUpdate();
        }
    }
}
