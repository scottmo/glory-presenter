package com.scottscmo.model.song.converters;

import com.scottscmo.model.song.Section;
import com.scottscmo.model.song.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Util {
    // TODO refactor this
    private static Map<String, List<String>> getSectionTextsByGroup(Song song, List<String> textGroups, int maxLines) {
        if (textGroups.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> data = new HashMap<>();
        for (String sectionName : song.order()) {
            Section section = song.section(sectionName);
            assert section != null :  "Unable to find section $sectionName";

            Map<String, List<String>> sectionText = section.textLines();

            // assuming all langs have same # of section lines
            int numSectionLines = getValueListSize(sectionText);
            int numSlidePerThisSection = 0;
            while (numSlidePerThisSection * maxLines < numSectionLines) {
                for (String group : textGroups) {
                    List<String> lines = data.getOrDefault(group, new ArrayList<>());
                    String line = "";
                    List<String> sectionLines = sectionText.get(group);
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

    public static List<Map<String, String>> getSectionTexts(Song song, List<String> textGroups, int maxLines) {
        Map<String, List<String>> distributedText = getSectionTextsByGroup(song, textGroups, maxLines);
        int numSections = getValueListSize(distributedText);
        return IntStream.range(0, numSections)
                .mapToObj(i -> textGroups.stream()
                        .filter(distributedText::containsKey)
                        .collect(Collectors.toMap(
                                group -> group,
                                group -> distributedText.get(group).get(i))))
                .collect(Collectors.toList());
    }

    private static int getValueListSize(Map<String, List<String>> map) {
        return map.values().stream().toList().get(0).size();
    }
}
