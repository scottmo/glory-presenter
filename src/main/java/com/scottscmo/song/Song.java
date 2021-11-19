package com.scottscmo.song;

import java.util.List;
import java.util.Map;

public record Song(
        String title,
        String collection,
        String description,
        List<Verse> lyrics,
        List<String> verseOrder) {

    public Map<String, String[]> getVerseText(String verseNum) {
        Verse verse = this.lyrics.stream()
                .filter(v -> v.verse().equals(verseNum))
                .findFirst().get();
        if (verse != null) {
            return verse.text();
        }
        return null;
    }
}
