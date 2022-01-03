package com.scottscmo.model.bible;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scottscmo.model.bible.BibleReference.VerseRange;

public class BibleModel {
    private static BibleModel model = null;
    public static BibleModel getInstance() {
        if (model == null) model = new BibleModel();
        return model;
    }

    private BibleVerseTable bibleVerseTable;
    private BookNamesTable bookNamesTable;

    private List<Map<String, String>> bookNames = null; // cache

    public BibleModel() {
        this.bibleVerseTable = new BibleVerseTable();
        this.bookNamesTable = new BookNamesTable();
    }

    public boolean insert(Map<String, List<List<String>>> bible, String version) {
        try {
            int insertedCount = this.bookNamesTable.insertBookNames(version, new ArrayList<>(bible.keySet()));
            System.out.println("Inserted " + insertedCount + " book names.");
        } catch (SQLException e) {
            System.err.println("Failed to insert book names!");
            e.printStackTrace();
            return false;
        }

        try {
            int insertedCount = this.bibleVerseTable.insertBibleVerses(version, bible);
            System.out.println("Inserted " + insertedCount + " bible verses.");
        } catch (SQLException e) {
            System.err.println("Failed to insert bible verses!");
            e.printStackTrace();
            return false;
        }

        this.bookNames = null;
        return true;
    }

    public Map<String, List<BibleVerse>> getBibleVerses(BibleReference ref) {
        if (ref == null) return null;

        String[] versions = ref.getVersions();
        String bookId = ref.getBook();
        try {
            Map<String, List<BibleVerse>> bibleVerses = new HashMap<>();
            for (String version : versions) {
                List<BibleVerse> verses = new ArrayList<>();
                for (VerseRange range : ref.getRanges()) {
                    verses.addAll(this.bibleVerseTable.getBibleVerses(version, bookId, range.chapter(), range.verses()));
                }
                bibleVerses.put(version, verses);
            }
            return bibleVerses;
        } catch (SQLException e) {
            System.err.println("Failed to get bible verses!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return { $bibleVersion: $bookName }
     */
    public Map<String, String> getBookNames(String bookId) {
        if (bookNames == null) {
            try {
                bookNames = this.bookNamesTable.getBookNames();
            } catch (SQLException e) {
                System.err.println("Failed to get book names!");
                e.printStackTrace();
            }
        }
        return bookNames.get(BibleMetadata.getBookIndex(bookId));
    }
}
