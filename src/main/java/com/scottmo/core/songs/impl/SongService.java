package com.scottmo.core.songs.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scottmo.core.appContext.impl.AppContextService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.impl.openLyrics.OpenLyricsConverter;
import com.scottmo.core.songs.impl.store.SongStore;

@Component
public class SongService {
    @Autowired
    private AppContextService appContextService;
    private SongStore store;
    private final OpenLyricsConverter openLyricsConverter = new OpenLyricsConverter();

    public SongStore getStore() {
        if (store == null) {
            store = new SongStore(Path.of(appContextService.getConfig().getDataDir()));
        }
        return store;
    }

    public OpenLyricsConverter getOpenLyricsConverter() {
        return openLyricsConverter;
    }

    public void importOpenLyricSong(File openLyricsFile) throws IOException {
        String openLyricsXML = Files.readString(openLyricsFile.toPath(), StandardCharsets.UTF_8);
        importOpenLyricSong(openLyricsXML);
    }

    public void importOpenLyricSong(String openLyricsXML) throws IOException {
        Song song = getOpenLyricsConverter().deserialize(openLyricsXML);
        store.store(song);
    }
}
