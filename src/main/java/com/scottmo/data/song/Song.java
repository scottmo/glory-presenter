package com.scottmo.data.song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Song {
    private static final String DEFAULT_LOCALE = Locale.getDefault().toString();

    private final Map<String, String> titles = new HashMap<>();
    private final List<String> authors = new ArrayList<>();
    private String publisher;
    private String copyright;
    private String songBook;
    private String entry;
    private String comments;
    private String verseOrder;
    private List<SongVerse> verses = new ArrayList<>();

    public String getTitle() {
        return this.titles.getOrDefault(DEFAULT_LOCALE, "");
    }

    public String getTitle(String locale) {
        String localeTitle = this.titles.get(locale);
        return localeTitle != null ? localeTitle : this.getTitle();
    }

    public void setTitle(String title) {
        setTitle(DEFAULT_LOCALE, title);
    }

    public void setTitle(String locale, String title) {
        if (locale == null) {
            locale = DEFAULT_LOCALE;
        }
        this.titles.put(locale, title);
    }

    public List<String> getTitleLocales() {
        List<String> locales = new ArrayList<>(this.titles.keySet());

        if (!locales.contains(DEFAULT_LOCALE)) {
            locales.add(DEFAULT_LOCALE);
        }

        return Collections.unmodifiableList(locales);
    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(this.authors);
    }

    public void addAuthor(String author) {
        this.authors.add(author);
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

    public String getSongBook() {
        return songBook;
    }

    public void setSongBook(String songBook) {
        this.songBook = songBook;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<String> getVerseLocales() {
        return verses.stream()
                .map(SongVerse::getLocale)
                .distinct()
                .toList();
    }

    public List<SongVerse> getVerses(String locale) {
        return verses.stream()
                .filter(verse -> verse.getLocale().equals(locale))
                .toList();
    }

    public List<SongVerse> getVerses() {
        List<SongVerse> verses = this.getVerses(DEFAULT_LOCALE);
        if (verses.isEmpty()) {
            List<String> locales = getVerseLocales();
            if (!locales.isEmpty()) {
                return this.getVerses(locales.get(0));
            }
        }
        return verses;
    }

    public List<String> getVerseNames() {
        List<SongVerse> verses = getVerses();
        if (verses != null) {
            return getVerses().stream().map(SongVerse::getName).toList();
        }
        return Collections.emptyList();
    }

    public List<String> getVerseOrderList() {
        if (verseOrder != null && !verseOrder.isEmpty()) {
            return Arrays.stream(verseOrder.split(" ")).toList();
        }
        return getVerseNames();
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }

    public void setVerses(List<SongVerse> verses) {
        this.verses = verses;
    }
}
