package com.scottmo.core.songs.impl.openLyrics.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class OpenLyricsTitle {
    @JacksonXmlProperty(isAttribute = true)
    private String lang;
    @JacksonXmlText
    private String value;

    public OpenLyricsTitle() {}

    public OpenLyricsTitle(String value) {
        this.value = value;
    }

    public OpenLyricsTitle(String lang, String value) {
        this.lang = lang;
        this.value = value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
