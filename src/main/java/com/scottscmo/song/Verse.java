package com.scottscmo.song;

import java.util.Map;
import java.util.stream.Collectors;

public record Verse(
    String verse,
    Map<String, String> text
) {
    public Map<String, String[]> getText() {
        return this.text.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue().split("\n")
            ));
    }
}
