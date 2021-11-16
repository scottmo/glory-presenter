package com.scottscmo.song.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scottscmo.song.Song;

public class SongSlideTextAdapter {
    private final static String DELIM = "\n\n---\n\n";

    public static List<String> serializeToList(Song song, List<String> langs, int linesPerSlidePerLang) {
        if (song == null || langs == null || langs.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> slides = new ArrayList<String>();

        for (Integer verseNumber : song.verseOrder()) {
            Map<String, String[]> verseText = song.getVerseText(verseNumber);

            assert (langs.stream().allMatch(lang -> verseText.containsKey(lang)));

            int numVerseLines = verseText.get(langs.get(0)).length;
            int numSlidePerThisVerse = 0;
            while (numSlidePerThisVerse * linesPerSlidePerLang < numVerseLines) {
                String slide = "";
                for (String lang : langs) {
                    String[] verseLines = verseText.get(lang);
                    if (verseLines == null) {
                        continue;
                    }
                    for (int i = 0; i < linesPerSlidePerLang; i++) {
                        int currLineInVerse = numSlidePerThisVerse * linesPerSlidePerLang + i;
                        if (currLineInVerse < verseLines.length) {
                            slide += verseLines[currLineInVerse] + "\n";
                        }
                    }
                }
                slides.add(slide.trim());
                numSlidePerThisVerse++;
            }   
        }

        return slides;
    }

    public static String serialize(Song song, List<String> langs, int linesPerSlidePerLang) {
        return serializeToList(song, langs, linesPerSlidePerLang).stream()
            .collect(Collectors.joining(DELIM));
    }
}
