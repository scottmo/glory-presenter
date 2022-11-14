package com.scottmo.data.song;

public class SongVerse {
    private String name;
    private String text;

    private String locale;

    public SongVerse() {}

    public SongVerse(String name, String text, String locale) {
        this.name = name;
        this.text = text;
        this.locale = locale;
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
        this.locale = locale;
    }
}
