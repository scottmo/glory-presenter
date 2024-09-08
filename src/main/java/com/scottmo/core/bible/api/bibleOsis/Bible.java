package com.scottmo.core.bible.api.bibleOsis;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Bible {
    @JacksonXmlProperty(localName = "osisIDWork", isAttribute = true)
    String id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "div")
    List<Book> books;

    Map<String, List<List<String>>> getVerses() {
        return books.stream()
                .collect(Collectors.toMap(
                    b -> BOOK_ID_MAP.get(b.id), Book::getVerses
                ));
    }

    // OSIS book ID to internal book ID
    private static Map<String, String> BOOK_ID_MAP = Stream.of(new String[][] {
            { "Gen", "genesis" },
            { "Exod", "exodus" },
            { "Lev", "leviticus" },
            { "Num", "numbers" },
            { "Deut", "deuteronomy" },
            { "Josh", "joshua" },
            { "Judg", "judges" },
            { "Ruth", "ruth" },
            { "1Sam", "1 samuel" },
            { "2Sam", "2 samuel" },
            { "1Kgs", "1 kings" },
            { "2Kgs", "2 kings" },
            { "1Chr", "1 chronicles" },
            { "2Chr", "2 chronicles" },
            { "Ezra", "ezra" },
            { "Neh", "nehemiah" },
            { "Esth", "esther" },
            { "Job", "job" },
            { "Ps", "psalms" },
            { "Prov", "proverbs" },
            { "Eccl", "ecclesiastes" },
            { "Song", "song of songs" },
            { "Isa", "isaiah" },
            { "Jer", "jeremiah" },
            { "Lam", "lamentations" },
            { "Ezek", "ezekiel" },
            { "Dan", "daniel" },
            { "Hos", "hosea" },
            { "Joel", "joel" },
            { "Amos", "amos" },
            { "Obad", "obadiah" },
            { "Jonah", "jonah" },
            { "Mic", "micah" },
            { "Nah", "nahum" },
            { "Hab", "habakkuk" },
            { "Zeph", "zephaniah" },
            { "Hag", "haggai" },
            { "Zech", "zechariah" },
            { "Mal", "malachi" },
            { "Matt", "matthew" },
            { "Mark", "mark" },
            { "Luke", "luke" },
            { "John", "john" },
            { "Acts", "acts" },
            { "Rom", "romans" },
            { "1Cor", "1 coriindexians" },
            { "2Cor", "2 coriindexians" },
            { "Gal", "galatians" },
            { "Eph", "ephesians" },
            { "Phil", "philippians" },
            { "Col", "colossians" },
            { "1Thess", "1 thessalonians" },
            { "2Thess", "2 thessalonians" },
            { "1Tim", "1 timothy" },
            { "2Tim", "2 timothy" },
            { "Titus", "titus" },
            { "Phlm", "philemon" },
            { "Heb", "hebrews" },
            { "Jas", "james" },
            { "1Pet", "1 peter" },
            { "2Pet", "2 peter" },
            { "1John", "1 john" },
            { "2John", "2 john" },
            { "3John", "3 john" },
            { "Jude", "jude" },
            { "Rev", "revelation" }
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
}
