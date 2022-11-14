package com.scottmo.data.song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private final Map<String, List<SongVerse>> lyrics = new HashMap<>();

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
        List<String> locales = new ArrayList<>(this.lyrics.keySet());

        if (!locales.contains(DEFAULT_LOCALE)) {
            locales.add(DEFAULT_LOCALE);
        }

        return Collections.unmodifiableList(locales);
    }

    public List<SongVerse> getVerses(String locale) {
        List<SongVerse> verses = this.lyrics.get(locale);
        return verses != null ? verses : this.lyrics.get(DEFAULT_LOCALE);
    }

    public List<SongVerse> getVerses() {
        List<SongVerse> verses = this.getVerses(DEFAULT_LOCALE);
        if (verses == null) {
            Set<String> keySet = this.lyrics.keySet();
            if (!keySet.isEmpty()) {
                String firstLocale = keySet.iterator().next();
                verses = this.getVerses(firstLocale);
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
        setVerses(DEFAULT_LOCALE, verses);
    }

    public void setVerses(String locale, List<SongVerse> verses) {
        this.lyrics.put(locale, verses);
    }

    public void addVerse(SongVerse newVerse) {
        addVerse(DEFAULT_LOCALE, newVerse);
    }

    public void addVerse(String locale, SongVerse newVerse) {
        List<SongVerse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
        verses.add(newVerse);
        this.lyrics.put(locale, verses);
    }

    public void addVerses(List<SongVerse> newVerses) {
        addVerses(DEFAULT_LOCALE, newVerses);
    }

    public void addVerses(String locale, List<SongVerse> newVerses) {
        List<SongVerse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
        verses.addAll(newVerses);
        this.lyrics.put(locale, verses);
    }

    public void removeVerse(int index) {
        removeVerse(Locale.getDefault(), index);
    }

    public void removeVerse(Locale locale, int index) {
        if (this.lyrics.containsKey(locale)) {
            this.lyrics.get(locale).remove(index);
        }
    }

    public void updateVerse(int index, String verseName, String verseText) {
        updateVerse(DEFAULT_LOCALE, index, verseName, verseText);
    }

    public void updateVerse(String locale, int index, String verseName, String verseText) {
        updateVerse(locale, index, verseName, Arrays.stream(verseText.split("\n")).toList());
    }

    public void updateVerse(String locale, int index, String verseName, List<String> lines) {
        if (this.lyrics.containsKey(locale)) {
            SongVerse verse = this.lyrics.get(locale).get(index);
            verse.setName(verseName);
            verse.setText(String.join("\n", lines));
        }
    }
}
