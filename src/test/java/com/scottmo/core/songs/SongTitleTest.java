package com.scottmo.core.songs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.scottmo.core.songs.api.song.SongTitle;

class SongTitleTest {

    @Test
    void constructor_setsFields() {
        SongTitle title = new SongTitle("Amazing Grace", "en_us");
        assertEquals("Amazing Grace", title.getText());
        assertEquals("en_us", title.getLocale());
    }

    @Test
    void constructor_normalizesLocale() {
        SongTitle title = new SongTitle("奇異恩典", "ZH-TW");
        assertEquals("zh_tw", title.getLocale());
    }

    @Test
    void setLocale_normalizesValue() {
        SongTitle title = new SongTitle();
        title.setLocale("EN-US");
        assertEquals("en_us", title.getLocale());
    }

    @Test
    void setText_updatesText() {
        SongTitle title = new SongTitle();
        title.setText("New Title");
        assertEquals("New Title", title.getText());
    }
}

