package com.scottmo.services.songsOpenLyrics.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collections;
import java.util.List;

public class OpenLyricsProperties {
    @JacksonXmlElementWrapper(localName = "titles")
    @JacksonXmlProperty(localName = "title")
    private List<OpenLyricsTitle> titles = Collections.emptyList();

    @JacksonXmlElementWrapper(localName = "authors")
    @JacksonXmlProperty(localName = "author")
    private List<String> authors = Collections.emptyList();

    private String copyright = "";
    private String publisher = "";

    @JacksonXmlElementWrapper(localName = "songbooks")
    @JacksonXmlProperty(localName = "songbook")
    private List<OpenLyricsSongBook> songbooks = Collections.emptyList();

    private String comments = "";
    private String verseOrder = "";

    public List<OpenLyricsTitle> getTitles() {
        return titles;
    }

    public void setTitles(List<OpenLyricsTitle> titles) {
        this.titles = titles;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public List<OpenLyricsSongBook> getSongbooks() {
        return songbooks;
    }

    public void setSongbooks(List<OpenLyricsSongBook> songbooks) {
        this.songbooks = songbooks;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }
}
