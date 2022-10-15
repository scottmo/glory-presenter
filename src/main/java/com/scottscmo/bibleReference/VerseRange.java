package com.scottscmo.bibleReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerseRange {
    private final int chapter;
    private final List<Integer> verses;
    VerseRange(int chapter, List<Integer> verses) {
        this.chapter = chapter;
        this.verses = verses;
    }

    static VerseRange of(String verseRangeString) {
        String[] rangeSplits = verseRangeString.split(":");
        int chapter = Integer.parseInt(rangeSplits[0]);
        List<Integer> verses = rangeSplits.length > 1
                ? parseVerseReferences(rangeSplits[1])
                : Collections.emptyList();
        return new VerseRange(chapter, verses);
    }

    private static List<Integer> parseVerseReferences(String verseRanges) {
        List<Integer> verses = new ArrayList<>();
        for (String verse : verseRanges.split(",")) {
            if (verse.isEmpty()) continue;
            if (verse.contains("-")) { // verse range
                String[] verseSplits = verse.split("-");
                int minVerse = Integer.parseInt(verseSplits[0]);
                int maxVerse = Integer.parseInt(verseSplits[1]);
                assert minVerse <= maxVerse : "Invalid verse range %s. End verse should not be less than start verse.".formatted(verse);
                for (int i = minVerse; i <= maxVerse; i++) {
                    verses.add(i);
                }
            } else { // single verse
                verses.add(Integer.parseInt(verse));
            }
        }
        return verses;
    }

    public int getChapter() {
        return chapter;
    }

    public List<Integer> getVerses() {
        return verses;
    }

    @Override
    public String toString() {
        if (this.chapter == 0) {
            return "";
        }
        if (this.verses.isEmpty()) {
            return String.valueOf(this.chapter);
        }
        List<String> verseRangeStrs = new ArrayList<>();
        int startVerse = this.verses.get(0);
        int endVerse = -1;
        for (int i = 1; i < this.verses.size(); i++) {
            if (this.verses.get(i - 1) + 1 == this.verses.get(i)) { // consecutive, update endVerse to cover current verse
                endVerse = this.verses.get(i);
            } else { // not consecutive
                verseRangeStrs.add(formatVerseReferences(startVerse, endVerse));
                startVerse = this.verses.get(i);
                endVerse = -1;
            }
        }
        verseRangeStrs.add(formatVerseReferences(startVerse, endVerse));
        return this.chapter + ":" + String.join(",", verseRangeStrs);
    }

    private String formatVerseReferences(int startVerse, int endVerse) {
        return endVerse <= startVerse
            ? String.valueOf(startVerse)
            : "%d-%d".formatted(startVerse, endVerse);
    }
}
