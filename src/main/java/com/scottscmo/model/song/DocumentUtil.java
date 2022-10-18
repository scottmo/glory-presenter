package com.scottscmo.model.song;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;

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

public class DocumentUtil {
    private static final ObjectMapper tomlMapper = new ObjectMapper(new TomlFactory());
    
    public static Document parseFromFuzzySearch(String dataPath, String titleSubstring) {
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

    public static Document parse(File file) throws IOException {
        return tomlMapper.readValue(file, Document.class);
    }

    public static Document parse(String content) throws IOException {
        return tomlMapper.readValue(content, Document.class);
    }

    public static String stringify(Document doc) throws JsonProcessingException {
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

    public static String stringify(Document document, List<String> textGroups, int maxLines) throws JsonProcessingException {
        List<Map<String, String>> transformedSectionTexts = getSectionTexts(document, textGroups, maxLines);
        Map<String, Map<String, String>> transformedSections = new HashMap<>();
        for (int i = 0; i < transformedSectionTexts.size(); i++) {
            transformedSections.put("s" + i, transformedSectionTexts.get(i));
        }
        document.setSections(transformedSections);
        document.setSectionOrder(transformedSections.keySet().stream().toList());
        dedupeSections(document);
        return stringify(document);
    }

    private static void dedupeSections(Document document) {
        List<String> newOrder = new ArrayList<>();
        Map<String, Map<String, String>> newSections = new HashMap<>();
        Map<String, String> visitedSections = new HashMap<>();
        for (var section : document.getSections().entrySet()) {
            // use section text as key and section number as value
            String sectionHash = section.getValue().entrySet().stream()
                    .map(text -> "%s=%s".formatted(text.getKey(), text.getValue()))
                    .collect(Collectors.joining("&"));
            if (visitedSections.containsKey(sectionHash)) {
                // if contains, meaning it's a dupe, reuse the section number
                newOrder.add(visitedSections.get(sectionHash));
            } else {
                // unique sections
                newOrder.add(section.getKey());
                newSections.put(section.getKey(), section.getValue());
                visitedSections.put(sectionHash, section.getKey());
            }
        }
        document.setSections(newSections);
        document.setSectionOrder(newOrder);
    }

    private static Map<String, List<String>> getSectionTextsByGroup(Document Document, List<String> textGroups, int maxLines) {
        if (textGroups.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> data = new HashMap<>();
        for (String sectionName : Document.getSectionOrder()) {
            Optional<Map<String, List<String>>> section = Document.getSection(sectionName);
            assert section.isPresent() :  "Unable to find section $sectionName";

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

    public static List<Map<String, String>> getSectionTexts(Document document, List<String> textGroups, int maxLines) {
        Map<String, List<String>> distributedText = getSectionTextsByGroup(document, textGroups, maxLines);
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
