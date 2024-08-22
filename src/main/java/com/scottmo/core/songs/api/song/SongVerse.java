package com.scottmo.core.songs.api.song;

import java.util.Locale;

import com.scottmo.shared.LocaleUtil;

public class SongVerse {
    private String name;
    private String text;

    private String locale;

    public SongVerse() {}

    public SongVerse(String name, String text, String locale) {
        this.name = name;
        this.text = text;
        this.locale = LocaleUtil.normalize(locale);
    }

    public SongVerse(String name, String text) {
        this(name, text, Locale.getDefault().toString());
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = LocaleUtil.normalize(locale);
    }
}
