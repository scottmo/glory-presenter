package com.scottmo.core.songs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongTitle;
import com.scottmo.core.songs.api.song.SongVerse;

import java.util.List;

class SongTest {

    private Song song;

    @BeforeEach
    void setUp() {
        song = new Song();
        song.setDefaultLocale("en_us");
    }

    @Test
    void setTitle_setsDefaultLocaleTitle() {
        song.setTitle("Amazing Grace");
        assertEquals("Amazing Grace", song.getTitle());
    }

    @Test
    void setTitle_withLocale_setsSpecificLocaleTitle() {
        song.setTitle("en_us", "Amazing Grace");
        song.setTitle("zh_tw", "奇異恩典");
        assertEquals("Amazing Grace", song.getTitle("en_us"));
        assertEquals("奇異恩典", song.getTitle("zh_tw"));
    }

    @Test
    void getTitle_normalizesLocale() {
        song.setTitle("en_us", "Amazing Grace");
        assertEquals("Amazing Grace", song.getTitle("EN-US"));
        assertEquals("Amazing Grace", song.getTitle("en-us"));
    }

    @Test
    void getTitle_returnsNull_forMissingLocale() {
        song.setTitle("en_us", "Amazing Grace");
        assertNull(song.getTitle("zh_tw"));
    }

    @Test
    void getPrimaryLocale_returnsDefaultLocale_whenPresent() {
        song.setDefaultLocale("en_us");
        song.setTitle("en_us", "Title");
        song.setTitle("zh_tw", "標題");
        assertEquals("en_us", song.getPrimaryLocale());
    }

    @Test
    void getPrimaryLocale_returnsFirstLocale_whenDefaultNotPresent() {
        song.setDefaultLocale("fr_fr");
        song.setTitle("en_us", "Title");
        song.setTitle("zh_tw", "標題");
        assertEquals("en_us", song.getPrimaryLocale());
    }

    @Test
    void getLocales_returnsAllLocales() {
        song.setTitle("en_us", "Title");
        song.setTitle("zh_tw", "標題");
        List<String> locales = song.getLocales();
        assertEquals(2, locales.size());
        assertTrue(locales.contains("en_us"));
        assertTrue(locales.contains("zh_tw"));
    }

    @Test
    void setAuthors_returnsUnmodifiableList() {
        song.setAuthors(List.of("Author1", "Author2"));
        List<String> authors = song.getAuthors();
        assertThrows(UnsupportedOperationException.class, () -> authors.add("Author3"));
    }

    @Test
    void getVerses_filtersByLocale() {
        song.setVerses(List.of(
            new SongVerse("v1", "Verse 1 English", "en_us"),
            new SongVerse("v1", "第一段中文", "zh_tw"),
            new SongVerse("v2", "Verse 2 English", "en_us")
        ));

        List<SongVerse> enVerses = song.getVerses("en_us");
        assertEquals(2, enVerses.size());

        List<SongVerse> zhVerses = song.getVerses("zh_tw");
        assertEquals(1, zhVerses.size());
    }

    @Test
    void getVerseNames_returnsAllVerseNames() {
        song.setVerses(List.of(
            new SongVerse("v1", "text", "en_us"),
            new SongVerse("v2", "text", "en_us"),
            new SongVerse("chorus", "text", "en_us")
        ));

        List<String> names = song.getVerseNames();
        assertEquals(List.of("v1", "v2", "chorus"), names);
    }

    @Test
    void fluentSetters_returnSong() {
        Song result = new Song()
            .setDefaultLocale("en_us")
            .setTitle("Title")
            .setPublisher("Publisher")
            .setCopyright("Copyright")
            .setSongBook("SongBook")
            .setEntry("001")
            .setComments("Comments")
            .setVerseOrder(List.of("v1", "chorus"));

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("Publisher", result.getPublisher());
        assertEquals("Copyright", result.getCopyright());
        assertEquals("SongBook", result.getSongBook());
        assertEquals("001", result.getEntry());
        assertEquals("Comments", result.getComments());
    }

    @Test
    void setters_trimInput() {
        song.setPublisher("  Publisher  ");
        song.setCopyright("  Copyright  ");
        song.setTitle("  Title  ");

        assertEquals("Publisher", song.getPublisher());
        assertEquals("Copyright", song.getCopyright());
        assertEquals("Title", song.getTitle());
    }

    @Test
    void getId_returnsNegativeOne_forNewSong() {
        assertEquals(-1, new Song().getId());
    }

    @Test
    void resetId_setsIdToNegativeOne() {
        Song s = new Song(42);
        assertEquals(42, s.getId());
        s.resetId();
        assertEquals(-1, s.getId());
    }
}

