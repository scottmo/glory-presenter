package com.scottscmo.model.song;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Song {

    private String title;
    private String tags;
    private List<Section> sections;
    private List<String> order;

    public String title() {
        return title;
    }

    public Song title(String title) {
        this.title = title;
        return this;
    }

    public String tags() {
        return tags;
    }

    public Song tags(String tags) {
        this.tags = tags;
        return this;
    }

    public List<Section> sections() {
        return sections;
    }

    public Song sections(List<Section> sections) {
        this.sections = sections.stream()
                .sorted(Comparator.comparing(Section::name))
                .toList();
        return this;
    }

    public Song sections(Map<String, Object> deserializedSections) {
        return sections(deserializedSections.entrySet().stream()
                .map(entry -> new Section()
                        .name(entry.getKey())
                        .text((Map<String, String>)entry.getValue()))
                .toList());
    }

    public List<String> order() {
        return order;
    }

    public Song order(List<String> order) {
        this.order = order;
        return this;
    }

    public Section section(String name) {
        if (name == null) return null;

        return this.sections.stream()
                .filter(section -> name.equals(section.name()))
                .findFirst()
                .orElse(null);
    }
}
