package com.scottmo.data.bibleOsis;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

class Book {
    @JacksonXmlProperty(localName = "osisID", isAttribute = true)
    String id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "chapter")
    List<Chapter> chapters;

    List<List<String>> getVerses() {
        return chapters.stream()
                .sorted((a, b) -> a.id.compareTo(b.id))
                .map(Chapter::getVerses)
                .toList();
    }
}
