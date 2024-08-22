package com.scottmo.core.bible.api.bibleOsis;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

class Verse {
    @JacksonXmlProperty(localName = "osisID", isAttribute = true)
    String id;

    @JacksonXmlText
    String text;
}
