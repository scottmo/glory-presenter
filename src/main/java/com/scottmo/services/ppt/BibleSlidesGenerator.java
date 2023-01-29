package com.scottmo.services.ppt;

import com.scottmo.data.bibleReference.BibleReference;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.bible.BibleVerse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.scottmo.services.ppt.TemplatingUtil.PLACEHOLDER_TEMPLATE;

public final class BibleSlidesGenerator {
    private static final Supplier<BibleStore> bibleStore = ServiceSupplier.get(BibleStore.class);
    private static final String VERSE_CHAPTER = "verse.chapter";
    private static final String VERSE_NUMBER = "verse.number";
    private static final String VERSE = "verse.%s";
    private static final String VERSE_RANGE = "verses";
    private static final String BOOK = "book.%s";

    public static void generate(String bibleRefString, String tmplFilePath, String outputFilePath,
            boolean hasStartSlide, boolean hasEndSlide) throws IOException {
        BibleReference bibleReference = new BibleReference(bibleRefString);
        List<Map<String, String>> slideContents = new ArrayList<>();

        Map<String, List<BibleVerse>> bibleVerses = bibleStore.get().getBibleVerses(bibleReference);
        Map<String, String> bookNames = bibleStore.get().getBookNames(bibleReference.getBook());

        Map<String, String> bibleMetadata = new HashMap<>();
        for (String version : bibleReference.getVersions()) {
            bibleMetadata.put(BOOK.formatted(version), bookNames.get(version));
        }
        bibleMetadata.put(VERSE_RANGE, bibleReference.getRangesString());

        if (hasStartSlide) {
            slideContents.add(bibleMetadata);
        }

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

        if (hasEndSlide) {
            slideContents.add(bibleMetadata);
        }

        TemplatingUtil.generateSlideShow(slideContents, hasStartSlide, hasEndSlide,
                PLACEHOLDER_TEMPLATE, tmplFilePath, outputFilePath);
    }
}
