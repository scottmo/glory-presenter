package com.scottmo.services.ppt;

import com.scottmo.data.song.Song;
import com.scottmo.data.song.SongVerse;
import com.scottmo.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SongSlidesGenerator {

    // placeholder keys
    private static final String VERSE_PREFIX = "verse.";
    private static final String TITLE_PREFIX = "title.";
    private static final String SONGBOOK = "songbook";
    private static final String ENTRY = "entry";
    private static final String COPYRIGHT = "copyright";
    private static final String PUBLISHER = "publisher";
    private static final String PLACEHOLDER_TEMPLATE = "{%s}";

    public static void generate(Song song, String tmplFilePath, String outputFilePath, List<String> locales,
            int maxLines, boolean hasStartSlide, boolean hasEndSlide) throws IOException {
        // song metadata for all slides
        Map<String, String> songMetadata = new HashMap<>();
        songMetadata.put(SONGBOOK, song.getSongBook());
        songMetadata.put(ENTRY, song.getEntry());
        songMetadata.put(COPYRIGHT, song.getCopyright());
        songMetadata.put(PUBLISHER, song.getPublisher());
        for (String locale: song.getLocales()) {
            songMetadata.put(TITLE_PREFIX + locale, song.getTitle(locale));
        }

        // lyrics
        List<String> textGroups = locales.stream().map(s -> VERSE_PREFIX + s).toList();
        List<Map<String, String>> lyrics = getSectionTexts(getOrderedVerses(song), textGroups, maxLines);
        // make song metadata also available to verse slides
        for (Map<String, String> verse : lyrics) {
            verse.putAll(songMetadata);
        }

        List<Map<String, String>> slideContents = new ArrayList<>();
        if (hasStartSlide) {
            slideContents.add(songMetadata);
        }
        slideContents.addAll(lyrics);
        if (hasEndSlide) {
            slideContents.add(songMetadata);
        }

        TemplatingUtil.generateSlideShow(slideContents, hasStartSlide, hasEndSlide,
                PLACEHOLDER_TEMPLATE, tmplFilePath, outputFilePath);
    }

    /**
     *
     * @return
     * [
     *     {
     *         [locale]: [
     *             "verse1line1",
     *             "verse1line2",
     *             ...
     *          ]
     *     }
     * ]
     */
    public static List<Map<String, List<String>>> getOrderedVerses(Song song) {
        List<Map<String, List<String>>> orderedVerses = new ArrayList<>(song.getVerseOrder().size());
        for (int i = 0; i < song.getVerseOrder().size(); i++) {
            orderedVerses.add(new HashMap<>());
        }

        Map<String, Integer> verseOrderMap = new HashMap<>();
        for (int i = 0; i < song.getVerseOrder().size(); i++) {
            verseOrderMap.put(song.getVerseOrder().get(i), i);
        }
        for (SongVerse verse : song.getVerses()) {
            Map<String, List<String>> verseGroup = orderedVerses.get(verseOrderMap.get(verse.getName()));
            verseGroup.put(VERSE_PREFIX + verse.getLocale(), StringUtils.split(verse.getText(), "\n"));
        }

        return orderedVerses;
    }

    private static int getValueListSize(Map<String, List<String>> map) {
        return map.values().stream().toList().get(0).size();
    }

    private static Map<String, List<String>> getSectionTextsByGroup(List<Map<String, List<String>>> orderedSectionTexts, List<String> textGroups, int maxLines) {
        if (textGroups.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> data = new HashMap<>();
        for (Map<String, List<String>> sectionTexts : orderedSectionTexts) {
            // assuming all langs have same # of section lines
            int numSectionLines = getValueListSize(sectionTexts);
            int numSlidePerThisSection = 0;
            while (numSlidePerThisSection * maxLines < numSectionLines) {
                for (String group : textGroups) {
                    List<String> lines = data.getOrDefault(group, new ArrayList<>());
                    String line = "";
                    List<String> sectionLines = sectionTexts.get(group);
                    if (sectionLines != null && !sectionLines.isEmpty()) {
                        for (int i = 0; i < maxLines; i++) {
                            int currLineInSection = numSlidePerThisSection * maxLines + i;
                            if (currLineInSection < sectionLines.size()) {
                                line += sectionLines.get(currLineInSection) + "\n";
                            }
                        }
                        lines.add(line.trim());
                        data.put(group, lines);
                    }
                }
                numSlidePerThisSection++;
            }
        }
        return data;
    }

    public static List<Map<String, String>> getSectionTexts(List<Map<String, List<String>>> orderedSectionTexts, List<String> textGroups, int maxLines) {
        Map<String, List<String>> distributedText = getSectionTextsByGroup(orderedSectionTexts, textGroups, maxLines);
        int numSections = getValueListSize(distributedText);
        return IntStream.range(0, numSections)
                .mapToObj(i -> textGroups.stream()
                        .filter(distributedText::containsKey)
                        .collect(Collectors.toMap(
                                group -> group,
                                group -> distributedText.get(group).get(i))))
                .collect(Collectors.toList());
    }
}
