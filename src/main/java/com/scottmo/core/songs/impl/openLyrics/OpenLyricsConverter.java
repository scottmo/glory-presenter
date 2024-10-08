package com.scottmo.core.songs.impl.openLyrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.core.songs.impl.openLyrics.model.OpenLyricsSong;
import com.scottmo.core.songs.impl.openLyrics.model.OpenLyricsSongBook;
import com.scottmo.core.songs.impl.openLyrics.model.OpenLyricsTitle;
import com.scottmo.core.songs.impl.openLyrics.model.OpenLyricsVerse;
import com.scottmo.shared.StringUtils;

import java.util.List;

public class OpenLyricsConverter {
    XmlMapper xmlMapper = new XmlMapper();

    public Song deserialize(String sourceXML) {
        try {
            sourceXML = sourceXML
                    .replaceAll("\n\s+", " ") // removes indentations and new lines
                    .replaceAll("\s*<\s*br\s*/>\s*", "\n"); // replace br's with \n

            OpenLyricsSong olsong = xmlMapper.readValue(sourceXML, OpenLyricsSong.class);
            Song song = new Song();
            for (OpenLyricsTitle title : olsong.getProperties().getTitles()) {
                song.setTitle(title.getLang(), title.getValue());
            }
            song.setAuthors(olsong.getProperties().getAuthors());
            song.setCopyright(olsong.getProperties().getCopyright());
            song.setPublisher(olsong.getProperties().getPublisher());
            if (!olsong.getProperties().getSongbooks().isEmpty()) {
                OpenLyricsSongBook songBook = olsong.getProperties().getSongbooks().get(0);
                song.setSongBook(songBook.getName());
                song.setEntry(songBook.getEntry());
            }
            song.setComments(olsong.getProperties().getComments());
            song.setVerseOrder(StringUtils.split(olsong.getProperties().getVerseOrder()));
            song.setVerses(olsong.getVerses().stream()
                    .map(v -> new SongVerse(v.getName(), v.getLines(), v.getLang())).toList());
            return song;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to load song from openLyrics format", e);
        }
    }

    public String serialize(Song song) {
        try {
            OpenLyricsSong olsong = new OpenLyricsSong();
            var titles = song.getLocales().stream()
                    .map(locale -> new OpenLyricsTitle(locale, song.getTitle(locale)))
                    .toList();
            olsong.getProperties().setTitles(titles);
            olsong.getProperties().setAuthors(song.getAuthors());
            olsong.getProperties().setCopyright(song.getCopyright());
            olsong.getProperties().setPublisher(song.getPublisher());
            olsong.getProperties().setSongbooks(List.of(new OpenLyricsSongBook(song.getSongBook(), song.getEntry())));
            olsong.getProperties().setComments(song.getComments());
            olsong.getProperties().setVerseOrder(String.join(",", song.getVerseOrder()));
            olsong.setVerses(song.getVerses().stream()
                    .map(v -> new OpenLyricsVerse(v.getName(), v.getLocale(), v.getText())).toList());

            return xmlMapper.writeValueAsString(olsong);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
