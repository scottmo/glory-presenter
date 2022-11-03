package com.scottmo.services.openLyrics;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OpenLyrics {
    private static final OpenLyricsDeserializer reader = new OpenLyricsDeserializer();
    private static final OpenLyricsSerializer writer = new OpenLyricsSerializer();

    private final Properties properties = new Properties();
    private final Map<Locale, List<Verse>> lyrics = new HashMap<>();

    public OpenLyrics() {}

    public static OpenLyrics of(String xmlSource) throws ParserConfigurationException, IOException, SAXException {
        return reader.deserialize(xmlSource);
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
        return getVerses().stream().map(Verse::getName).toList();
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
            verse.setLines(lines);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return writer.serialize(this);
    }
}
