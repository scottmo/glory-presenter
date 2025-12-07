package com.scottmo.core.bible;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.scottmo.core.bible.api.bibleReference.BibleReference;
import com.scottmo.core.bible.api.bibleReference.VerseRange;

import java.util.Collections;
import java.util.List;

class VerseRangeTest {

    private VerseRange getVerseRange(String ref) {
        return new BibleReference(ref).getRanges().get(0);
    }

    @Test
    void of_parsesChapterOnly() {
        VerseRange range = getVerseRange("john 3");
        assertEquals(3, range.getChapter());
        assertEquals(Collections.emptyList(), range.getVerses());
    }

    @Test
    void of_parsesSingleVerse() {
        VerseRange range = getVerseRange("john 3:16");
        assertEquals(3, range.getChapter());
        assertEquals(List.of(16), range.getVerses());
    }

    @Test
    void of_parsesMultipleVerses() {
        VerseRange range = getVerseRange("john 3:1,5,10");
        assertEquals(3, range.getChapter());
        assertEquals(List.of(1, 5, 10), range.getVerses());
    }

    @Test
    void of_parsesVerseRange() {
        VerseRange range = getVerseRange("john 3:1-5");
        assertEquals(3, range.getChapter());
        assertEquals(List.of(1, 2, 3, 4, 5), range.getVerses());
    }

    @Test
    void of_parsesMixedVersesAndRanges() {
        VerseRange range = getVerseRange("john 3:1,3-5,8");
        assertEquals(3, range.getChapter());
        assertEquals(List.of(1, 3, 4, 5, 8), range.getVerses());
    }

    @Test
    void toString_formatsChapterOnly() {
        VerseRange range = getVerseRange("john 5");
        assertEquals("5", range.toString());
    }

    @Test
    void toString_formatsSingleVerse() {
        VerseRange range = getVerseRange("john 3:16");
        assertEquals("3:16", range.toString());
    }

    @Test
    void toString_formatsNonConsecutiveVerses() {
        VerseRange range = getVerseRange("john 3:1,5,10");
        assertEquals("3:1,5,10", range.toString());
    }

    @Test
    void toString_compressesConsecutiveVerses() {
        VerseRange range = getVerseRange("john 3:1-5");
        assertEquals("3:1-5", range.toString());
    }

    @Test
    void toString_formatsMixedVersesAndRanges() {
        VerseRange range = getVerseRange("john 3:1,3-5,8");
        assertEquals("3:1,3-5,8", range.toString());
    }
}

