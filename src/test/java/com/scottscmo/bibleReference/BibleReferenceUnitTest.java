package com.scottscmo.bibleReference;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import java.util.List;

public class BibleReferenceUnitTest {
    @Test
    public void parseBibleVersions() {
        var br = new BibleReference("cuv,niv - john 1");
        assertEquals(List.of("cuv", "niv"), br.getVersions());
    }

    @Test
    public void parseBookChapter() {
        var br = new BibleReference("john 1");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
    }

    @Test
    public void parseBookChapterVerse() {
        var br = new BibleReference("john 1:5");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5), br.getRanges().get(0).getVerses());
    }

    @Test
    public void parseBookChapterVerses() {
        var br = new BibleReference("john 1:5,8,10");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5, 8, 10), br.getRanges().get(0).getVerses());
    }

    @Test
    public void parseBookChapterVerseRange() {
        var br = new BibleReference("john 1:2,5-7,9");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(2, 5, 6, 7, 9), br.getRanges().get(0).getVerses());
    }

    @Test
    public void parseBookChapterVerseRanges() {
        var br = new BibleReference("john 1:5-7;7:3-5");
        assertEquals("john", br.getBook());
        assertEquals(2, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5, 6, 7), br.getRanges().get(0).getVerses());
        assertEquals(7, br.getRanges().get(1).getChapter());
        assertEquals(List.of(3, 4, 5), br.getRanges().get(1).getVerses());
    }

    @Test
    public void formatBookChapter() {
        var brStr = "john 1";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    public void formatBookChapterVerse() {
        var brStr = "john 1:5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    public void formatBookChapterVerses() {
        var brStr = "john 1:5,8,10";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    public void formatBookChapterVerseRange() {
        var brStr = "john 1:2,5-7,9";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    public void formatBookChapterVerseRanges() {
        var brStr = "john 1:5-7;7:3-5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }
}