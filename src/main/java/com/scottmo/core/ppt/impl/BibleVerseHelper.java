package com.scottmo.core.ppt.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleReference.BibleReference;

class BibleVerseHelper {
    // placeholder keys
    private static final String VERSE_CHAPTER = "verse.chapter";
    private static final String VERSE_NUMBER = "verse.number";
    private static final String VERSE = "verse.%s";
    private static final String VERSE_RANGE = "verses";
    private static final String BOOK = "book.%s";

    private ConfigService configService = ConfigService.get();
    private BibleService bibleService = ServiceProvider.get(BibleService.class).get();

    List<Map<String, String>> toSlideContents(String bibleRefString) {

        List<Map<String, String>> slideContents = new ArrayList<>();

        // missing version
        if (!bibleRefString.contains(BibleReference.VERSION_SEPERATOR)) {
            bibleRefString = getBibleVersionsString() + BibleReference.VERSION_SEPERATOR + bibleRefString;
        }

        BibleReference bibleReference = new BibleReference(bibleRefString);
        Map<String, List<BibleVerse>> bibleVerses = bibleService.getBibleVerses(bibleReference);
        Map<String, String> bookNames = bibleService.getBookNames(bibleReference.getBook());

        Map<String, String> bibleMetadata = new HashMap<>();
        for (String version : bibleReference.getVersions()) {
            bibleMetadata.put(BOOK.formatted(version), bookNames.get(version));
        }
        bibleMetadata.put(VERSE_RANGE, bibleReference.getRangesString());

        // title/end slide
        slideContents.add(bibleMetadata);

        // verse slides
        int numVerses = bibleVerses.values().iterator().next().size();
        for (int i = 0; i < numVerses; i++) {
            Map<String, String> verseMap = new HashMap<>();
            for (String version : bibleReference.getVersions()) {
                verseMap.put(VERSE.formatted(version), bibleVerses.get(version).get(i).text());
            }
            BibleVerse verse = bibleVerses.get(bibleReference.getVersions().get(0)).get(i);
            verseMap.put(VERSE_CHAPTER, String.valueOf(verse.chapter()));
            verseMap.put(VERSE_NUMBER, String.valueOf(verse.index()));
            verseMap.putAll(bibleMetadata);
            slideContents.add(verseMap);
        }

        return slideContents;
    }

    private String getBibleVersionsString() {
        return String.join(",", new ArrayList<>(configService.getConfig().getBibleVersionToLocale().keySet()));
    }
}
