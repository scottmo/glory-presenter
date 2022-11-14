package com.scottmo.data.song;

public class SongVerse {
    private String name;
    private String text;

    public SongVerse() {}

    public SongVerse(String name, String text) {
        this.name = name;
        this.text = text;
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
}
