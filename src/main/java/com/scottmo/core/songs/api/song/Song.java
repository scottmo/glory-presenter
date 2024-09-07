package com.scottmo.core.songs.api.song;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scottmo.shared.LocaleUtil;
import com.scottmo.shared.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Song {
    private int id = -1;

    private List<SongTitle> titles = new ArrayList<>();
    private List<String> authors = new ArrayList<>();
    private String publisher = "";
    private String copyright = "";
    private String songBook = "";
    private String entry = "";
    private String comments = "";
    private List<String> verseOrder = new ArrayList<>();
    private List<SongVerse> verses = new ArrayList<>();

    private String defaultLocale;

    public Song() {}

    public Song(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * @return primary locale used in this song. If no locale or default locale is present, default locale
     * is the primary one, otherwise the first locale available.
     */
    @JsonIgnore
    public String getPrimaryLocale() {
        List<String> locales = this.getLocales();
        if (locales.isEmpty() || locales.contains(defaultLocale)) {
            return defaultLocale;
        }
        return locales.get(0);
    }

    public Song setDefaultLocale(String locale) {
        this.defaultLocale = locale;
        return this;
    }

    public List<SongTitle> getTitles() {
        return titles;
    }

    @JsonIgnore
    public String getTitle() {
        return getTitle(getPrimaryLocale());
    }

    public String getTitle(String locale) {
        locale = LocaleUtil.normalize(locale);
        for (SongTitle title : this.titles) {
            if (title.getLocale().equals(locale)) {
                return title.getText();
            }
        }
        return null;
    }

    public Song setTitle(String title) {
        setTitle(getPrimaryLocale(), title);
        return this;
    }

    public Song setTitle(String locale, String title) {
        if (locale == null) {
            locale = getPrimaryLocale();
        } else {
            locale = LocaleUtil.normalize(locale);
        }
        this.titles.add(new SongTitle(StringUtils.trim(title), locale));
        return this;
    }

    @JsonIgnore
    public List<String> getLocales() {
        return this.titles.stream()
            .map(SongTitle::getLocale)
            .collect(Collectors.toList());
    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(this.authors);
    }

    public Song setAuthors(List<String> authors) {
        this.authors = authors;
        return this;
    }

    public String getPublisher() {
        return publisher;
    }

    public Song setPublisher(String publisher) {
        this.publisher = StringUtils.trim(publisher);
        return this;
    }

    public String getCopyright() {
        return copyright;
    }

    public Song setCopyright(String copyright) {
        this.copyright = StringUtils.trim(copyright);
        return this;
    }

    public String getSongBook() {
        return songBook;
    }

    public Song setSongBook(String songBook) {
        this.songBook = StringUtils.trim(songBook);
        return this;
    }

    public String getEntry() {
        return entry;
    }

    public Song setEntry(String entry) {
        this.entry = StringUtils.trim(entry);
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Song setComments(String comments) {
        this.comments = StringUtils.trim(comments);
        return this;
    }

    public List<SongVerse> getVerses(String locale) {
        return verses.stream()
                .filter(verse -> verse.getLocale().equals(locale))
                .toList();
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    @JsonIgnore
    public List<String> getVerseNames() {
        List<SongVerse> verses = getVerses();
        if (verses != null) {
            return getVerses().stream().map(SongVerse::getName).toList();
        }
        return Collections.emptyList();
    }

    public List<String> getVerseOrder() {
        return verseOrder;
    }

    public Song setVerseOrder(List<String> verseOrder) {
        this.verseOrder = verseOrder;
        return this;
    }

    public Song setVerses(List<SongVerse> verses) {
        this.verses = verses;
        return this;
    }
}
