package com.scottmo.services.songsImportExport;

import com.scottmo.data.song.Song;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class SongsImportExportService {
    private final OpenLyricsDeserializer openLyricsDeserializer = new OpenLyricsDeserializer();
    private final OpenLyricsSerializer openLyricsSerializer = new OpenLyricsSerializer();

    public Song deserializeFromOpenLyrics(String sourceXML) {
        try {
            return openLyricsDeserializer.deserialize(sourceXML);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Failed to load song from openLyrics format", e);
        }
    }

    public String serializeToOpenLyrics(Song song) {
        return openLyricsSerializer.serialize(song);
    }
}
