package com.scottmo.data.openLyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Verse {
    private String name;
    private List<String> lines = new ArrayList<>();

    public Verse() {}

    public Verse(String name, List<String> lines) {
        this.name = name;
        this.lines = lines;
    }

    public void addLine(String line) {
        this.lines.add(line);
    }

    public void setLines(List<String> lines) {
        this.lines = Objects.requireNonNullElseGet(lines, ArrayList::new);
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(this.lines);
    }

    public String getLinesAsString() {
        return String.join("\n", this.lines);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
