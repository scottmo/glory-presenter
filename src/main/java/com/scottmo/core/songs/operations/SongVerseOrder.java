package com.scottmo.core.songs.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.scottmo.core.songs.api.song.SongVerse;

/** Reusable equivalent of the Song Editor's Update Verse Order action. */
public final class SongVerseOrder {
    private SongVerseOrder() {}

    public static List<String> generate(List<SongVerse> verses) {
        if (verses == null) {
            throw new IllegalArgumentException("Song verses are required");
        }
        TreeSet<String> verseNames = new TreeSet<>();
        TreeSet<String> chorusNames = new TreeSet<>();
        for (SongVerse verse : verses) {
            if (verse == null || verse.getName() == null) {
                throw new IllegalArgumentException("Song verses must have names");
            }
            String name = verse.getName();
            if (name.startsWith("v")) {
                verseNames.add(name);
            } else if (name.startsWith("c")) {
                chorusNames.add(name);
            }
        }

        List<String> order = new ArrayList<>();
        for (String verse : verseNames) {
            order.add(verse);
            order.addAll(chorusNames);
        }
        return order;
    }
}
