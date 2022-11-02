package com.scottmo.services.openLyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Verse {
    private String name;
    private final List<String> lines = new ArrayList<>();

    public void addLine(String line) {
        this.lines.add(line);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(this.lines);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
