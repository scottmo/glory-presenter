package com.scottmo.services.songsOpenLyrics.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class OpenLyricsVerse {
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    @JacksonXmlProperty(isAttribute = true)
    private String lang;
    private String lines;

    public OpenLyricsVerse() {}

    public OpenLyricsVerse(String name, String lang, String lines) {
        this.name = name;
        this.lang = lang;
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines.trim();
    }
}
