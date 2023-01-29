package com.scottmo.data.bibleOsis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Map;

@JacksonXmlRootElement(localName = "osis")
public class Osis {
    @JacksonXmlElementWrapper(localName = "osisText")
    Bible bible;

    public String getId() {
        return bible.id.toLowerCase();
    }

    public Map<String, List<List<String>>> getVerses() {
        return bible.getVerses();
    }

    public static Osis of(String osisXML) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper.readValue(osisXML, Osis.class);
    }
}
