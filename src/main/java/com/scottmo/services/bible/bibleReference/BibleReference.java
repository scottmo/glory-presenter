package com.scottmo.services.bible.bibleReference;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * e.g. cuv,niv - john 1:2-3;3:4;5:1-3
 */
public class BibleReference {
    private static final Pattern RE_VERSE_NOTATION = Pattern.compile("(\\d?[A-z\\s]+)\\s+([\\d,;:\\-\\s]+)");

    private List<String> versions;
    private String book;
    private List<VerseRange> ranges;

    public BibleReference(String bibleReferenceStr) {
        assert !bibleReferenceStr.isEmpty() : "BibleReference is missing completely!";
        String refString = bibleReferenceStr;

        String[] bibleReferenceStrParts = refString.split(" - ");
        if (bibleReferenceStrParts.length > 1) {
            versions = List.of(bibleReferenceStrParts[0].split(","));
            refString = bibleReferenceStrParts[1];
        }

        Matcher matcher = RE_VERSE_NOTATION.matcher(refString);
        if (matcher.find() && matcher.groupCount() == 2) {
            book = matcher.group(1).trim().replaceAll("\\s+", " ").toLowerCase();

            String rangesStr = matcher.group(2).trim().replaceAll("\\s+", "");
            ranges = Arrays.stream(rangesStr.split(";"))
                    .filter(s -> !s.isEmpty())
                    .map(VerseRange::of)
                    .toList();
        }
        assert book != null && !book.isEmpty() : "BibleReference '%s' is missing book name".formatted(bibleReferenceStr);
        assert ranges != null && !ranges.isEmpty() : "BibleReference '%s' is missing verse ranges".formatted(bibleReferenceStr);
    }

    public List<String> getVersions() {
        return versions;
    }

    public String getBook() {
        return book;
    }

    public List<VerseRange> getRanges() {
        return ranges;
    }

    public String getRangesString() {
        return this.ranges.stream()
                .map(VerseRange::toString)
                .collect(Collectors.joining(";"));
    }

    @Override
    public String toString() {
        return this.ranges.isEmpty()
                ? this.book
                : this.book + " " + getRangesString();
    }
}