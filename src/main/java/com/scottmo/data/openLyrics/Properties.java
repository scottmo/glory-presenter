package com.scottmo.data.openLyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Properties {
    private final Map<Locale, String> title = new HashMap<>();
    private String copyright;
    private List<String> verseOrder = new ArrayList<>();
    private final List<String> authors = new ArrayList<>();
    private String publisher;
    private final List<String> comments = new ArrayList<>();

    public void setTitle(String title) {
        setTitle(Locale.getDefault(), title);
    }

    public void setTitle(Locale locale, String title) {
        this.title.put(locale, title);
    }

    public String getTitle() {
        return this.title.get(Locale.getDefault());
    }

    public String getTitle(Locale locale) {
        return this.title.get(locale) != null ? this.title.get(locale) : this.getTitle();
    }

    public List<Locale> getTitleLocales() {
        List<Locale> locales = new ArrayList<>(this.title.keySet());

        if (!locales.contains(Locale.getDefault())) {
            locales.add(Locale.getDefault());
        }

        return Collections.unmodifiableList(locales);
    }

    public void addAuthor(String author) {
        this.authors.add(author);
    }

    public List<String> getAuthors() {
        return Collections.unmodifiableList(this.authors);
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(this.comments);
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    public void addVerseOrder(String name) {
        if (this.verseOrder == null) {
            this.verseOrder = new ArrayList<>();
        }

        this.verseOrder.add(name);
    }

    public final void setVerseOrder(List<String> verseOrder) {
        this.verseOrder = Objects.requireNonNullElseGet(verseOrder, ArrayList::new);
    }

    public List<String> getVerseOrder() {
        return this.verseOrder;
    }
}
