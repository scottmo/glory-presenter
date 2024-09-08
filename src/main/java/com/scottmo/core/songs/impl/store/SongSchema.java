package com.scottmo.core.songs.impl.store;

final class SongSchema {
    
    static final String DB_NAME = "songs";

    static class SongTable {
        static final String TABLE = "songs";
        static final String ID = "id";
        static final String AUTHORS = "authors";
        static final String COPYRIGHT = "copyright";
        static final String PUBLISHER = "publisher";
        static final String SONGBOOK = "songbook";
        static final String SONGBOOK_ENTRY = "entry";
        static final String COMMENTS = "comments";
        static final String VERSE_ORDER = "verseOrder";

        static String createTable() {
            return String.join(" ",
                    "CREATE TABLE IF NOT EXISTS %s (".formatted(TABLE),
                        "%s INTEGER PRIMARY KEY AUTOINCREMENT,".formatted(ID),
                        "%s TEXT,".formatted(AUTHORS),
                        "%s TEXT,".formatted(COPYRIGHT),
                        "%s TEXT,".formatted(PUBLISHER),
                        "%s TEXT,".formatted(SONGBOOK),
                        "%s TEXT,".formatted(SONGBOOK_ENTRY),
                        "%s TEXT,".formatted(COMMENTS),
                        "%s TEXT".formatted(VERSE_ORDER),
                    ")"
            );
        }
    }

    static class TitlesTable {
        static final String TABLE = "titles";
        static final String SONG_ID = "song_id";
        static final String TEXT = "text";
        static final String LOCALE = "locale";

        static String createTable() {
            return String.join(" ",
                    "CREATE TABLE IF NOT EXISTS %s (".formatted(TABLE),
                        "%s INTEGER NOT NULL REFERENCES %s (%s),".formatted(SONG_ID, SongTable.TABLE, SongTable.ID),
                        "%s TEXT NOT NULL,".formatted(TEXT),
                        "%s TEXT NOT NULL,".formatted(LOCALE),
                        "PRIMARY KEY (%s, %s)".formatted(SONG_ID, LOCALE),
                    ")"
            );
        }
    }

    static class VersesTable {
        static final String TABLE = "verses";
        static final String SONG_ID = "song_id";
        static final String NAME = "name";
        static final String TEXT = "text";
        static final String LOCALE = "locale";

        static String createTable() {
            return String.join(" ",
                    "CREATE TABLE IF NOT EXISTS %s (".formatted(TABLE),
                    "%s INTEGER NOT NULL REFERENCES %s (%s),".formatted(SONG_ID, SongTable.TABLE, SongTable.ID),
                    "%s TEXT NOT NULL,".formatted(NAME),
                    "%s TEXT NOT NULL,".formatted(TEXT),
                    "%s TEXT NOT NULL,".formatted(LOCALE),
                    "PRIMARY KEY (%s, %s, %s)".formatted(SONG_ID, NAME, LOCALE),
                    ")"
            );
        }
    }
}
