package com.scottmo.services.songs.openLyrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "song")
public class OpenLyricsSong {
    @JacksonXmlProperty(isAttribute = true)
    private final String xmlns = "http://openlyrics.info/namespace/2009/song";
    @JacksonXmlProperty(isAttribute = true)
    private final String version = "0.9";

    private OpenLyricsProperties properties = new OpenLyricsProperties();

    @JacksonXmlElementWrapper(localName = "lyrics")
    @JacksonXmlProperty(localName = "verse")
    private List<OpenLyricsVerse> verses = Collections.emptyList();

    public OpenLyricsProperties getProperties() {
        return properties;
    }

    public void setProperties(OpenLyricsProperties properties) {
        this.properties = properties;
    }

    public List<OpenLyricsVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<OpenLyricsVerse> verses) {
        this.verses = verses;
    }
}
