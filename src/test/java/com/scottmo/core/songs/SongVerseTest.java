package com.scottmo.core.songs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.scottmo.core.songs.api.song.SongVerse;

class SongVerseTest {

    @Test
    void constructor_setsFields() {
        SongVerse verse = new SongVerse("v1", "Amazing grace how sweet the sound", "en_us");
        assertEquals("v1", verse.getName());
        assertEquals("Amazing grace how sweet the sound", verse.getText());
        assertEquals("en_us", verse.getLocale());
    }

    @Test
    void constructor_normalizesLocale() {
        SongVerse verse = new SongVerse("v1", "text", "EN-US");
        assertEquals("en_us", verse.getLocale());
    }

    @Test
    void setLocale_normalizesValue() {
        SongVerse verse = new SongVerse();
        verse.setLocale("ZH-TW");
        assertEquals("zh_tw", verse.getLocale());
    }

    @Test
    void defaultConstructor_allowsLaterSetting() {
        SongVerse verse = new SongVerse();
        verse.setName("chorus");
        verse.setText("This is the chorus");
        verse.setLocale("en_us");

        assertEquals("chorus", verse.getName());
        assertEquals("This is the chorus", verse.getText());
        assertEquals("en_us", verse.getLocale());
    }
}

