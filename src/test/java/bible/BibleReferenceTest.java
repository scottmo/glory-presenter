package bible;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import com.scottscmo.model.bible.BibleReference;

import org.junit.jupiter.api.Test;

class BibleReferenceTest {
    @Test
    void parseBookChapter() {
        BibleReference br = new BibleReference("john 1");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).chapter());
    }

    @Test
    void parseBookChapterVerse() {
        BibleReference br = new BibleReference("john 1:5");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).chapter());
        assertTrue(Arrays.equals(new int[]{5}, br.getRanges().get(0).verses()));
    }

    @Test
    void parseBookChapterVerses() {
        BibleReference br = new BibleReference("john 1:5,8,10");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).chapter());
        assertTrue(Arrays.equals(new int[]{5, 8, 10}, br.getRanges().get(0).verses()));
    }

    @Test
    void parseBookChapterVerseRange() {
        BibleReference br = new BibleReference("john 1:2,5-7,9");
        assertEquals("john", br.getBook());
        assertEquals(1, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).chapter());
        assertTrue(Arrays.equals(new int[]{2, 5, 6, 7, 9}, br.getRanges().get(0).verses()));
    }

    @Test
    void parseBookChapterVerseRanges() {
        BibleReference br = new BibleReference("john 1:5-7;7:3-5");
        assertEquals("john", br.getBook());
        assertEquals(2, br.getRanges().size());
        assertEquals(1, br.getRanges().get(0).chapter());
        assertTrue(Arrays.equals(new int[]{5, 6, 7}, br.getRanges().get(0).verses()));
        assertEquals(7, br.getRanges().get(1).chapter());
        assertTrue(Arrays.equals(new int[]{3, 4, 5}, br.getRanges().get(1).verses()));
    }

    @Test
    void formatBookChapter() {
        String brStr = "john 1";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerse() {
        String brStr = "john 1:5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerses() {
        String brStr = "john 1:5,8,10";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerseRange() {
        String brStr = "john 1:2,5-7,9";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }

    @Test
    void formatBookChapterVerseRanges() {
        String brStr = "john 1:5-7;7:3-5";
        assertEquals(brStr, new BibleReference(brStr).toString());
    }
}
