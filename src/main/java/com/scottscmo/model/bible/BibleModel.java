package com.scottscmo.model.bible;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BibleModel {
    private static BibleModel model = null;
    public static BibleModel getInstance() {
        if (model == null) model = new BibleModel();
        return model;
    }

    private BibleVerseTable bibleVerseTable;
    private BookNamesTable bookNamesTable;

    public BibleModel() {
        this.bibleVerseTable = new BibleVerseTable();
        this.bookNamesTable = new BookNamesTable();
    }

    public boolean insert(Map<String, List<List<String>>> bible, String version) {
        try {
            int insertedCount = this.bookNamesTable.insertBookNames(version, new ArrayList<>(bible.keySet()));
            System.out.println("Inserted " + insertedCount + " book names.");
        } catch (SQLException e) {
            System.err.println("Failed to insert book names");
            e.printStackTrace();
            return false;
        }

        try {
            int insertedCount = this.bibleVerseTable.insertBibleVerses(version, bible);
            System.out.println("Inserted " + insertedCount + " bible verses.");
        } catch (SQLException e) {
            System.err.println("Failed to insert bible verses");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
