package com.scottscmo.model.song;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Section {

    private String name;
    private Map<String, String> text;

    public String name() {
        return name;
    }

    public Section name(String name) {
        this.name = name;
        return this;
    }

    public Map<String, String> text() {
        return text;
    }

    public Section text(Map<String, String> text) {
        this.text = text;
        return this;
    }

    public Map<String, List<String>> textLines() {
        return this.text.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> Arrays.asList(e.getValue().split("\n"))));
    }
}
