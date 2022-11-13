package com.scottmo.services.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import com.scottmo.data.content.Content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentUtil {
    private static final ObjectMapper tomlMapper = new ObjectMapper(new TomlFactory());
    
    public static Content parseFromFuzzySearch(String dataPath, String titleSubstring) {
        File[] files = new File(Path.of(dataPath).toString()).listFiles();
        if (files != null) {
            Optional<String> fileName = Arrays.stream(files)
                    .map(File::getName)
                    .sorted()
                    .filter(name -> name.toLowerCase().contains(titleSubstring))
                    .findFirst();
            if (fileName.isPresent()) {
                try {
                    File file = Path.of(dataPath, fileName.get()).toFile();
                    return parse(file);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load document by " + titleSubstring, e);
                }
            }
        }
        return null;
    }

    public static Content parse(File file) throws IOException {
        return tomlMapper.readValue(file, Content.class);
    }

    public static Content parse(String content) throws IOException {
        return tomlMapper.readValue(content, Content.class);
    }

    public static String stringify(Content doc) throws JsonProcessingException {
        return "[title]\n"
                + tomlMapper.writeValueAsString(doc.getTitle())
                + "\n[metadata]\n"
                + tomlMapper.writeValueAsString(doc.getMetadata())
                + "\n[sections]\n"
                + doc.getSections().entrySet().stream()
                .map(section -> {
                    String sectionValue = section.getValue().entrySet().stream()
                            .map(sectionEntry ->
                                    "%s = \"\"\"\n%s\n\"\"\"".formatted(sectionEntry.getKey(), sectionEntry.getValue().trim()))
                            .collect(Collectors.joining("\n"));
                    return "\n[sections.%s]\n%s".formatted(section.getKey(), sectionValue);
                })
                .collect(Collectors.joining("\n"));
    }

    public static String stringify(Content content, List<String> textGroups, int maxLines) throws JsonProcessingException {
        List<Map<String, String>> transformedSectionTexts = getSectionTexts(content, textGroups, maxLines);
        Map<String, Map<String, String>> transformedSections = new HashMap<>();
        List<String> sectionOrder = new ArrayList<>();
        for (int i = 0; i < transformedSectionTexts.size(); i++) {
            String key = "s" + i;
            sectionOrder.add(key);
            transformedSections.put(key, transformedSectionTexts.get(i));
        }
        content.setSections(transformedSections);
        content.setSectionOrder(sectionOrder);
        dedupeSections(content);
        return stringify(content);
    }

    private static void dedupeSections(Content content) {
        List<String> newOrder = new ArrayList<>();
        Map<String, Map<String, String>> newSections = new HashMap<>();
        Map<String, String> visitedSections = new HashMap<>();
        for (String sectionName : content.getSectionOrder()) {
            var section = content.getSections().get(sectionName);
            // use section text as key and section number as value
            String sectionHash = section.entrySet().stream()
                    .map(text -> "%s=%s".formatted(text.getKey(), text.getValue()))
                    .collect(Collectors.joining("&"));
            if (visitedSections.containsKey(sectionHash)) {
                // if contains, meaning it's a dupe, reuse the section number
                newOrder.add(visitedSections.get(sectionHash));
            } else {
                // unique sections
                newOrder.add(sectionName);
                newSections.put(sectionName, section);
                visitedSections.put(sectionHash, sectionName);
            }
        }
        content.setSections(newSections);
        content.setSectionOrder(newOrder);
    }

    private static Map<String, List<String>> getSectionTextsByGroup(Content Content, List<String> textGroups, int maxLines) {
        if (textGroups.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> data = new HashMap<>();
        for (String sectionName : Content.getSectionOrder()) {
            Optional<Map<String, List<String>>> section = Content.getSection(sectionName);
            if (section.isEmpty()) {
                throw new RuntimeException("Unable to find section " + sectionName);
            }

            // assuming all langs have same # of section lines
            int numSectionLines = getValueListSize(section.get());
            int numSlidePerThisSection = 0;
            while (numSlidePerThisSection * maxLines < numSectionLines) {
                for (String group : textGroups) {
                    List<String> lines = data.getOrDefault(group, new ArrayList<>());
                    String line = "";
                    List<String> sectionLines = section.get().get(group);
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

    public static List<Map<String, String>> getSectionTexts(Content content, List<String> textGroups, int maxLines) {
        Map<String, List<String>> distributedText = getSectionTextsByGroup(content, textGroups, maxLines);
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
