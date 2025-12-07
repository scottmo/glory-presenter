package com.scottmo.core.bible;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.scottmo.core.bible.api.bibleReference.BibleReference;

import java.util.List;

class BibleReferenceTest {
    @Test
    void parseBibleVersions() {
        var br = new BibleReference("cuv,niv - john 1");
        assertEquals(List.of("cuv", "niv"), br.getVersions());
    }

    @Test
    void parseBookChapter() {
        var br = new BibleReference("john 1");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
    }

    @Test
    void parseBookChapterVerse() {
        var br = new BibleReference("john 1:5");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5), br.getRanges().get(0).getVerses());
    }

    @Test
    void parseBookChapterVerses() {
        var br = new BibleReference("john 1:5,8,10");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5, 8, 10), br.getRanges().get(0).getVerses());
    }

    @Test
    void parseBookChapterVerseRange() {
        var br = new BibleReference("john 1:2,5-7,9");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(2, 5, 6, 7, 9), br.getRanges().get(0).getVerses());
    }

    @Test
    void parseBookChapterVerseRanges() {
        var br = new BibleReference("john 1:5-7;7:3-5");
        assertEquals("john", br.getBook());
        assertEquals(2, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).getChapter());
        assertEquals(List.of(5, 6, 7), br.getRanges().get(0).getVerses());
        assertEquals(7, br.getRanges().get(1).getChapter());
        assertEquals(List.of(3, 4, 5), br.getRanges().get(1).getVerses());
    }

    @Test
    void formatBookChapter() {
        var brStr = "john 1";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerse() {
        var brStr = "john 1:5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerses() {
        var brStr = "john 1:5,8,10";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerseRange() {
        var brStr = "john 1:2,5-7,9";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerseRanges() {
        var brStr = "john 1:5-7;7:3-5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }
}

