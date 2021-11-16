package com.scottscmo.song;

import java.util.Map;

public record Verse(
        Integer verse,
        Map<String, String[]> text) {
}
