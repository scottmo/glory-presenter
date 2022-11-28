package com.scottmo.services.songs.openLyrics.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class OpenLyricsSongBook {
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    @JacksonXmlProperty(isAttribute = true)
    private String entry;

    public OpenLyricsSongBook() {}

    public OpenLyricsSongBook(String name, String entry) {
        this.name = name;
        this.entry = entry;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
