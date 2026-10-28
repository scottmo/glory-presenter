package com.scottmo.core.bible.impl.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.bible.api.bibleReference.BibleReference;

class BibleStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void queriesLegacyTextBookIndexesStoredAsDecimalStrings() throws Exception {
        int johnIndex = BibleMetadata.getBookIndex("john");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("bible.db"));
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE bible_niv (bookIndex TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, text TEXT NOT NULL, PRIMARY KEY(bookIndex, chapter, verse))");
            statement.executeUpdate("INSERT INTO bible_niv VALUES ('" + johnIndex + ".0', 1, 1, 'Legacy verse text')");
        }

        try (BibleStore store = new BibleStore(tempDir)) {
            var result = store.getBibleVerses(new BibleReference("niv - john 1:1"));

            assertEquals(List.of("Legacy verse text"),
                result.get("niv").stream().map(verse -> verse.text()).toList());
        }
    }

    @Test
    void retainsLocalizedBookNamesFromExistingDatabaseRows() throws Exception {
        int johnIndex = BibleMetadata.getBookIndex("john");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("bible.db"));
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE book_names (id INTEGER NOT NULL, name VARCHAR(30) NOT NULL, version VARCHAR(10) NOT NULL, PRIMARY KEY (name, version))");
            statement.executeUpdate("INSERT INTO book_names VALUES (" + johnIndex + ", '約翰福音', 'cuv')");
        }

        try (BibleStore store = new BibleStore(tempDir)) {
            assertEquals(java.util.Map.of("cuv", "約翰福音"), store.getBookNames("john"));
        }
    }
}
