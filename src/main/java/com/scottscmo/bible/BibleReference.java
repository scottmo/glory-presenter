package com.scottscmo.bible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BibleReference {
    private static final Pattern RE_VERSE_NOTATION =
            Pattern.compile("([\\d]?[A-z\\s]+)\\s+([\\d,;:\\-\\s]+)");

    private String book;
    private List<VerseRange> ranges;

    public BibleReference(String bibleReferenceStr) {
        if (bibleReferenceStr == null || bibleReferenceStr.isEmpty()) {
            throw new IllegalArgumentException("Missing verseNotation!");
        }

        String book = null;
        String rangesStr = null;
        Matcher matcher = RE_VERSE_NOTATION.matcher(bibleReferenceStr);
        if (matcher.find() && matcher.groupCount() == 2) {
            book = matcher.group(1).trim().replaceAll("\\s+", " ").toLowerCase();
            rangesStr = matcher.group(2).trim().replaceAll("\\s+", "");
        } else {
            throw new IllegalArgumentException("Missing book name or verse ranges!");
        }

        List<VerseRange> ranges = Stream.of(rangesStr.split(";"))
            .filter(s -> !s.isEmpty())
            .map(range -> {
                String[] rangeSplits = range.split(":");
                int chapter = Integer.parseInt(rangeSplits[0]);
                List<Integer> verses = (rangeSplits.length > 1)
                    ? parseVerseRanges(rangeSplits[1])
                    : Collections.emptyList();
                return new VerseRange(chapter, verses.stream().mapToInt(i->i).toArray());
            })
            .collect(Collectors.toList());

        this.book = book;
        this.ranges = ranges;
    }

    public String getBook() {
        return this.book;
    }

    public List<VerseRange> getRanges() {
        return this.ranges;
    }

    public String toString() {
        if (this.book == null || this.book.isEmpty()) {
            return "";
        }

        if (this.ranges == null || this.ranges.isEmpty()) {
            return this.book;
        }

        return String.format("%s %s",
            book,
            ranges.stream().map(VerseRange::toString).collect(Collectors.joining(";"))
        );
    }

    private List<Integer> parseVerseRanges(String verseRanges) {
        List<Integer> verses = new ArrayList<>();
        for (String verse : verseRanges.split(",")) {
            if (verse.isEmpty()) continue;

            if (verse.contains("-")) { // verse range
                String[] verseSplits = verse.split("-");
                int minVerse = Integer.parseInt(verseSplits[0]);
                int maxVerse = Integer.parseInt(verseSplits[1]);
                if (minVerse > maxVerse) {
                    throw new IllegalArgumentException("Invalid verse range: " + verse 
                        + ". End verse should not be less than start verse.");
                }
                for (int i = minVerse; i <= maxVerse; i++) {
                    verses.add(i);
                }
            } else { // single verse
                verses.add(Integer.parseInt(verse));
            }
        }
        return verses;
    }

    public record VerseRange(
        int chapter,
        int[] verses
    ) {
        public String toString() {
            if (this.chapter == 0) {
                return "";
            }
            if (this.verses == null || this.verses.length == 0) {
                return this.chapter + "";
            }

            List<String> verseRangeStrs = new ArrayList<>();
            int startVerse = this.verses[0];
            int endVerse = -1;
            for (int i = 1; i < this.verses.length; i++) {
                if (this.verses[i-1] + 1 == this.verses[i]) { // consective, update endVerse to cover current verse
                    endVerse = this.verses[i];
                } else { // not consective
                    verseRangeStrs.add(formatVerseRange(startVerse, endVerse));
                    startVerse = this.verses[i];
                    endVerse = -1;
                }
            }
            verseRangeStrs.add(formatVerseRange(startVerse, endVerse));
            return this.chapter + ":" + verseRangeStrs.stream().collect(Collectors.joining(","));
        }

        private String formatVerseRange(int startVerse, int endVerse) {
            return endVerse <= startVerse
                ? startVerse + ""
                : startVerse + "-" + endVerse;
        }
    }
}
