package com.scottscmo.model.song;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Document {
    private Map<String, String> title;
    private Map<String, String> metadata;
    private Map<String, Map<String, String>> sections;

    public Map<String, String> getTitle() {
        return title;
    }

    public Document setTitle(Map<String, String> title) {
        this.title = title;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Document setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @JsonIgnore
    public List<String> getSectionOrder() {
        String order = metadata.getOrDefault("order", "");
        return Arrays.stream(order.split(",")).map(String::trim).toList();
    }

    public Document setSectionOrder(List<String> order) {
        this.metadata.put("order", String.join(", ", order));
        return this;
    }

    public Map<String, Map<String, String>> getSections() {
        return sections;
    }

    public Document setSections(Map<String, Map<String, String>> sections) {
        this.sections = sections;
        return this;
    }

    private Optional<Map<String, String>> getRawSection(String name) {
        return this.sections.entrySet().stream()
                .filter(section -> name.equals(section.getKey()))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    public Optional<Map<String, List<String>>> getSection(String name) {
        return getRawSection(name).map(section -> section.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> Arrays.asList(e.getValue().split("\n")))));
    }

    @Override
    public String toString() {
        return "Document(\n"
                + "\ttitle=" + title.toString() + ",\n"
                + "\tmetadata=" + metadata.toString() + ",\n"
                + "\tsections=" + sections.toString() + ",\n"
                + ")";
    }
}
