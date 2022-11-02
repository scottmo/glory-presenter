package com.scottmo.services.openLyrics;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
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
            verses = this.getVerses(new Locale("en", "US"));
        }
        if (verses == null) {
            Set<Locale> keySet = this.lyrics.keySet();
            if (!keySet.isEmpty()) {
                Locale firstLocale = keySet.iterator().next();
                verses = this.getVerses(firstLocale);
            }
        }
        return verses;
    }

    public void addVerse(Locale locale, Verse newVerse) {
        List<Verse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
        verses.add(newVerse);
        this.lyrics.put(locale, verses);
    }

    public void addVerses(Locale locale, List<Verse> newVerses) {
        List<Verse> verses = this.lyrics.getOrDefault(locale, new ArrayList<>());
        verses.addAll(newVerses);
        this.lyrics.put(locale, verses);
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return writer.serialize(this);
    }
}
