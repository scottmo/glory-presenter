package com.scottmo.data.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Content {
    private Map<String, String> title;
    private Map<String, String> metadata;
    private Map<String, Map<String, String>> sections;

    public Map<String, String> getTitle() {
        return title;
    }

    public Content setTitle(Map<String, String> title) {
        this.title = title;
        return this;
    }

    @JsonIgnore
    public String getJoinedTitle(List<String> order) {
        String index = this.getMetadata().getOrDefault("index", "");
        return Stream.concat(Stream.of(index), order.stream().map(key -> this.getTitle().get(key)))
                .filter(s -> !Strings.isNullOrEmpty(s))
                .collect(Collectors.joining(" "));
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Content setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @JsonIgnore
    public List<String> getSectionOrder() {
        String order = metadata.getOrDefault("order", "");
        return Arrays.stream(order.split(",")).map(String::trim).toList();
    }

    public Content setSectionOrder(List<String> order) {
        this.metadata.put("order", String.join(", ", order));
        return this;
    }

    public Map<String, Map<String, String>> getSections() {
        return sections;
    }

    public Content setSections(Map<String, Map<String, String>> sections) {
        this.sections = sections;
        return this;
    }

    public Optional<Map<String, String>> getRawSection(String name) {
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
