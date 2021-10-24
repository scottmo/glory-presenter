package com.scottscmo.song;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlideTextTransformer {
    public static List<String> transform(Song song, List<String> langs, int linesPerSlidePerLang) {
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

    public static void main(String[] args) throws IOException {
        String path = "test.yaml";
        String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
        Song song = SongObjectMapper.deserialize(content);
        String output = transform(song, Arrays.asList("zh", "en"), 3).stream()
                .collect(Collectors.joining("\n\n---\n\n"));
        System.out.println(output);
    }
}
