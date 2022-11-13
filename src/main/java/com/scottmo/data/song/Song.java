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
    private final Map<Locale, String> titles = new HashMap<>();
    private final List<String> authors = new ArrayList<>();
    private String publisher;
    private String copyright;
    private String comments;
    private List<String> verseOrder = new ArrayList<>();
    private final Map<Locale, List<Verse>> lyrics = new HashMap<>();

    public String getTitle() {
        return this.titles.get(Locale.getDefault());
    }

    public String getTitle(Locale locale) {
        return this.titles.get(locale) != null ? this.titles.get(locale) : this.getTitle();
    }

    public void setTitle(String title) {
        setTitle(Locale.getDefault(), title);
    }

    public void setTitle(Locale locale, String title) {
        this.titles.put(locale, title);
    }

    public List<Locale> getTitleLocales() {
        List<Locale> locales = new ArrayList<>(this.titles.keySet());

        if (!locales.contains(Locale.getDefault())) {
            locales.add(Locale.getDefault());
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Map<Locale, List<Verse>> getLyrics() {
        return lyrics;
    }

    public List<Verse> getVerses(Locale locale) {
        List<Verse> verses = this.lyrics.get(locale);
        return verses != null ? verses : this.lyrics.get(Locale.getDefault());
    }

    public List<Verse> getVerses() {
        List<Verse> verses = this.getVerses(Locale.getDefault());
        if (verses == null) {
            Set<Locale> keySet = this.lyrics.keySet();
            if (!keySet.isEmpty()) {
                Locale firstLocale = keySet.iterator().next();
                verses = this.getVerses(firstLocale);
            }
        }
        return verses;
    }

    public List<String> getVerseNames() {
        List<Verse> verses = getVerses();
        if (verses != null) {
            return getVerses().stream().map(Verse::getName).toList();
        }
        return Collections.emptyList();
    }

    public List<String> getVerseOrder() {
        if (verseOrder != null && !verseOrder.isEmpty()) {
            return verseOrder;
        }
        return getVerseNames();
    }

    public void setVerseOrder(List<String> verseOrder) {
        this.verseOrder = verseOrder;
    }

    public void setVerses(List<Verse> verses) {
        setVerses(Locale.getDefault(), verses);
    }

    public void setVerses(Locale locale, List<Verse> verses) {
        this.lyrics.put(locale, verses);
    }

    public void addVerse(Verse newVerse) {
        addVerse(Locale.getDefault(), newVerse);
    }

    public void addVerse(Locale locale, Verse newVerse) {
        List<Verse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
        verses.add(newVerse);
        this.lyrics.put(locale, verses);
    }

    public void addVerses(List<Verse> newVerses) {
        addVerses(Locale.getDefault(), newVerses);
    }

    public void addVerses(Locale locale, List<Verse> newVerses) {
        List<Verse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
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
        updateVerse(Locale.getDefault(), index, verseName, verseText);
    }

    public void updateVerse(Locale locale, int index, String verseName, String verseText) {
        updateVerse(locale, index, verseName, Arrays.stream(verseText.split("\n")).toList());
    }

    public void updateVerse(Locale locale, int index, String verseName, List<String> lines) {
        if (this.lyrics.containsKey(locale)) {
            Verse verse = this.lyrics.get(locale).get(index);
            verse.setName(verseName);
            verse.setText(String.join("\n", lines));
        }
    }
}
