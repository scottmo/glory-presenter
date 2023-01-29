package com.scottmo.data.bibleOsis;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

class Chapter {
    @JacksonXmlProperty(localName = "osisID", isAttribute = true)
    String id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "verse")
    List<Verse> verses;

    List<String> getVerses() {
        return verses.stream()
                .sorted((a, b) -> a.id.compareTo(b.id))
                .map(v -> v.text)
                .toList();
    }
}
