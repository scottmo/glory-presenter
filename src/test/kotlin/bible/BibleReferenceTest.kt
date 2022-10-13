package bible

import com.scottscmo.bibleReference.BibleReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BibleReferenceTest {
    @Test
    fun parseBookChapter() {
        val br = BibleReference("john 1")
        assertEquals("john", br.book)
        assertEquals(1, br.ranges.size)
        assertEquals(1, br.ranges[0].chapter)
    }

    @Test
    fun parseBookChapterVerse() {
        val br = BibleReference("john 1:5")
        assertEquals("john", br.book)
        assertEquals(1, br.ranges.size)
        assertEquals(1, br.ranges[0].chapter)
        assertTrue(listOf(5) == br.ranges[0].verses)
    }

    @Test
    fun parseBookChapterVerses() {
        val br = BibleReference("john 1:5,8,10")
        assertEquals("john", br.book)
        assertEquals(1, br.ranges.size)
        assertEquals(1, br.ranges[0].chapter)
        assertTrue(listOf(5, 8, 10) == br.ranges[0].verses)
    }

    @Test
    fun parseBookChapterVerseRange() {
        val br = BibleReference("john 1:2,5-7,9")
        assertEquals("john", br.book)
        assertEquals(1, br.ranges.size)
        assertEquals(1, br.ranges[0].chapter)
        assertTrue(listOf(2, 5, 6, 7, 9) == br.ranges[0].verses)
    }

    @Test
    fun parseBookChapterVerseRanges() {
        val br = BibleReference("john 1:5-7;7:3-5")
        assertEquals("john", br.book)
        assertEquals(2, br.ranges.size)
        assertEquals(1, br.ranges[0].chapter)
        assertTrue(listOf(5, 6, 7) == br.ranges[0].verses)
        assertEquals(7, br.ranges[1].chapter)
        assertTrue(listOf(3, 4, 5) == br.ranges[1].verses)
    }

    @Test
    fun formatBookChapter() {
        val brStr = "john 1"
        assertEquals(brStr, BibleReference(brStr).toString())
    }

    @Test
    fun formatBookChapterVerse() {
        val brStr = "john 1:5"
        assertEquals(brStr, BibleReference(brStr).toString())
    }

    @Test
    fun formatBookChapterVerses() {
        val brStr = "john 1:5,8,10"
        assertEquals(brStr, BibleReference(brStr).toString())
    }

    @Test
    fun formatBookChapterVerseRange() {
        val brStr = "john 1:2,5-7,9"
        assertEquals(brStr, BibleReference(brStr).toString())
    }

    @Test
    fun formatBookChapterVerseRanges() {
        val brStr = "john 1:5-7;7:3-5"
        assertEquals(brStr, BibleReference(brStr).toString())
    }
}