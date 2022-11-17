package com.scottmo.services.songs;

import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysObjects;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.scottmo.util.SqlUtils;

final class SongSchema {

    SongTable song;
    TitleTable titles;
    VerseTable verses;

    SongSchema() {
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();
        song = new SongTable(schema);
        titles = new TitleTable(schema, song);
        verses = new VerseTable(schema, song);
    }

    class SongTable {
        DbTable table;
        DbColumn id;
        DbColumn authors;
        DbColumn copyright;
        DbColumn publisher;
        DbColumn songbook;
        DbColumn entry;
        DbColumn comments;
        DbColumn verseOrder;

        SongTable(DbSchema schema) {
            table = schema.addTable("songs");
            id = table.addColumn("id", "INTEGER", null);
            id.primaryKey();
            authors = table.addColumn("authors", "TEXT", null);
            copyright = table.addColumn("copyright", "TEXT", null);
            publisher = table.addColumn("publisher", "TEXT", null);
            songbook = table.addColumn("songbook", "TEXT", null);
            entry = table.addColumn("entry", "TEXT", null);
            comments = table.addColumn("comments", "TEXT", null);
            verseOrder = table.addColumn("verseOrder", "TEXT", null);
        }
    }
    String createSongTable() {
        return new CreateTableQuery(song.table, true)
                .addCustomization(MysObjects.IF_NOT_EXISTS_TABLE)
                .addColumnConstraint(song.id, SqlUtils.AUTO_INCREMENT)
                .validate().toString();
    }

    class TitleTable {
        DbTable table;
        DbColumn songId;
        DbColumn text;
        DbColumn locale;

        TitleTable(DbSchema schema, SongTable songTable) {
            table = schema.addTable("titles");
            songId = table.addColumn("song_id", "INTEGER", null);
            songId.references(null, songTable.table, songTable.id);
            songId.notNull();
            text = table.addColumn("text", "TEXT", null);
            text.notNull();
            locale = table.addColumn("locale", "TEXT", null);
            locale.notNull();
            table.primaryKey(null, songId.getName(), locale.getName());
        }
    }
    String createTitleTable() {
        return new CreateTableQuery(titles.table, true)
                .addCustomization(MysObjects.IF_NOT_EXISTS_TABLE)
                .validate().toString();
    }

    class VerseTable {
        DbTable table;
        DbColumn songId;
        DbColumn name;
        DbColumn text;
        DbColumn locale;

        VerseTable(DbSchema schema, SongTable songTable) {
            table = schema.addTable("verses");
            songId = table.addColumn("song_id", "INTEGER", null);
            songId.references(null, songTable.table, songTable.id);
            name = table.addColumn("name", "TEXT", null);
            name.notNull();
            text = table.addColumn("text", "TEXT", null);
            text.notNull();
            locale = table.addColumn("locale", "TEXT", null);
            locale.notNull();
            table.primaryKey(null, songId.getName(), name.getName(), locale.getName());
        }
    }
    String createVerseTable() {
        return new CreateTableQuery(verses.table, true)
                .addCustomization(MysObjects.IF_NOT_EXISTS_TABLE)
                .validate().toString();
    }
}
