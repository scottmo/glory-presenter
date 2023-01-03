package com.scottmo.data.song;

import com.scottmo.util.LocaleUtil;
import com.scottmo.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scottmo.config.AppContext.PRIMARY_LOCALE;

public class Song {
    private int id = -1;

    private final Map<String, String> titles = new HashMap<>();
    private List<String> authors = new ArrayList<>();
    private String publisher = "";
    private String copyright = "";
    private String songBook = "";
    private String entry = "";
    private String comments = "";
    private String verseOrder = "";
    private List<SongVerse> verses = new ArrayList<>();

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
    public String getPrimaryLocale() {
        List<String> locales = this.getLocales();
        if (locales.isEmpty() || locales.contains(PRIMARY_LOCALE)) {
            return PRIMARY_LOCALE;
        }
        return locales.get(0);
    }

    public String getTitle() {
        return this.titles.get(getPrimaryLocale());
    }

    public String getTitle(String locale) {
        locale = LocaleUtil.normalize(locale);
        return this.titles.getOrDefault(locale, this.getTitle());
    }

    public void setTitle(String title) {
        setTitle(getPrimaryLocale(), title);
    }

    public void setTitle(String locale, String title) {
        if (locale == null) {
            locale = getPrimaryLocale();
        } else {
            locale = LocaleUtil.normalize(locale);
        }
        this.titles.put(locale, StringUtils.trim(title));
    }

    public List<String> getLocales() {
        List<String> locales = new ArrayList<>(this.titles.keySet());
        return Collections.unmodifiableList(locales);
    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(this.authors);
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = StringUtils.trim(publisher);
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = StringUtils.trim(copyright);
    }

    public String getSongBook() {
        return songBook;
    }

    public void setSongBook(String songBook) {
        this.songBook = StringUtils.trim(songBook);
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = StringUtils.trim(entry);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = StringUtils.trim(comments);
    }

    public List<SongVerse> getVerses(String locale) {
        return verses.stream()
                .filter(verse -> verse.getLocale().equals(locale))
                .toList();
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    public List<String> getVerseNames() {
        List<SongVerse> verses = getVerses();
        if (verses != null) {
            return getVerses().stream().map(SongVerse::getName).toList();
        }
        return Collections.emptyList();
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = StringUtils.normalizeListString(verseOrder, true);
    }

    public void setVerses(List<SongVerse> verses) {
        this.verses = verses;
    }
}
