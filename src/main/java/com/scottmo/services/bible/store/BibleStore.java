package com.scottmo.services.bible.store;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.data.bibleReference.BibleReference;
import com.scottmo.data.bibleReference.VerseRange;

public final class BibleStore {
    private static final String DB_NAME = "bible";

    private final BibleVerseTable bibleVerseTable;
    private final BookNamesTable bookNamesTable;
    private List<Map<String, String>> bookNames = Collections.emptyList(); // cache

    public BibleStore(Path storeLocation) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + storeLocation.resolve("%s.db".formatted(DB_NAME)));
            bibleVerseTable = new BibleVerseTable(conn);
            bookNamesTable = new BookNamesTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insert(Map<String, List<List<String>>> bible, String version) {

        try {
            int insertedCount = bookNamesTable.insert(version, new ArrayList<>(bible.keySet()));
            System.out.printf("Inserted %d book names.%n", insertedCount);
        } catch (SQLException e) {
            System.err.println("Failed to insert book names!");
            e.printStackTrace();
            return 0;
        }

        try {
            int insertedVerseCount = bibleVerseTable.insert(version, bible);
            System.out.printf("Inserted %d bible verses.%n", insertedVerseCount);
            bookNames = Collections.emptyList();
            return insertedVerseCount;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert bible verses!\n", e);
        }
    }

    public Map<String, List<BibleVerse>> getBibleVerses(BibleReference ref) {
        List<String> versions = ref.getVersions();
        String bookId = ref.getBook();
        try {
            Map<String, List<BibleVerse>> bibleVerses = new HashMap<>();
            for (String version : versions) {
                List<BibleVerse> verses = new ArrayList<>();
                for (VerseRange range : ref.getRanges()) {
                    verses.addAll(bibleVerseTable.query(version, bookId, range.getChapter(), range.getVerses()));
                }
                bibleVerses.put(version, verses);
            }
            return bibleVerses;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get bible verses!\n", e);
        }
    }

    public List<String> getAvailableVersions() {
        try {
            return this.bookNamesTable.queryVersions();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query available bible versions", e);
        }
    }

    /**
     * @return { $bibleVersion $bookName }
     */
    public Map<String, String> getBookNames(String bookId) {
        if (this.bookNames.isEmpty()) {
            try {
                this.bookNames = this.bookNamesTable.queryAll();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get book names!", e);
            }
        }
        int bookIndex = BibleMetadata.getBookIndex(bookId);
        return bookIndex >= 0
                ? this.bookNames.get(bookIndex)
                : Collections.emptyMap();
    }
}