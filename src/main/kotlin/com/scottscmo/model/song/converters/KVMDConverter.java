package com.scottscmo.model.song.converters;

import com.scottscmo.model.song.Section;
import com.scottscmo.model.song.Song;
import com.scottscmo.util.KVMD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class KVMDConverter {
    public static Song parse(String kvmdSong) {
        if (kvmdSong == null) return null;

        Map<String, Object> kvmdObj = KVMD.parse(kvmdSong);
        return new Song()
                .title(KVMD.getNamespace(kvmdObj))
                .tags((String)(KVMD.getMetadata(kvmdObj).getOrDefault("tags", "")))
                .sections(KVMD.getContent(kvmdObj))
                .order((List<String>)(KVMD.getMetadata(kvmdObj).getOrDefault("order", Collections.emptyList())));
    }

    public static String stringify(Song song) {
        return KVMD.stringify(KVMD.create(
            song.title(),
            Map.of(
                "order", song.order(),
                "tags", song.tags()
            ),
            song.sections().stream()
                    .collect(Collectors.toMap(Section::name, Section::text))
        ));
    }

    public static String stringify(Song song, List<String> textGroups, int maxLines) {
        List<Map<String, String>> transformedSectionTexts = Util.getSectionTexts(song, textGroups, maxLines);
        List<Section> transformedSections = new ArrayList<>();
        for (int i = 0; i < transformedSectionTexts.size(); i++) {
            transformedSections.add(new Section()
                    .name("s" + i)
                    .text(transformedSectionTexts.get(i)));
        }
        song.sections(transformedSections);
        song.order(transformedSections.stream().map(Section::name).toList());
        dedupeSections(song);
        return stringify(song);
    }

    private static void dedupeSections(Song song) {
        List<String> newOrder = new ArrayList<>();
        List<Section> newSections = new ArrayList<>();
        Map<String, String> visitedSections = new HashMap<>();
        for (int i = 0; i < song.sections().size(); i++) {
            Section section = song.sections().get(i);
            // use section text as key and section number as value
            String sectionKey = section.text().entrySet().stream()
                    .map(text -> "%s=%s".formatted(text.getKey(), text.getValue()))
                    .collect(Collectors.joining("&"));
            if (visitedSections.containsKey(sectionKey)) {
                // if contains, meaning it's a dupe, reuse the section number
                newOrder.add(visitedSections.get(sectionKey));
            } else {
                // unique sections
                newOrder.add(section.name());
                newSections.add(section);
                visitedSections.put(sectionKey, section.name());
            }
        }
        song.sections(newSections);
        song.order(newOrder);
    }
}
