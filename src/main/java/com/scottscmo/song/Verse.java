package com.scottscmo.song;

import java.util.Map;

public record Verse(
        String verse,
        Map<String, String[]> text) {
}
