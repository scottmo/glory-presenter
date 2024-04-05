package com.scottmo.services.songs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scottmo.data.song.Song;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.songs.openLyrics.OpenLyricsConverter;
import com.scottmo.services.songs.store.SongStore;

@Component
public class SongService {
    @Autowired
    private AppContextService appContextService;
    private SongStore store;
    private final OpenLyricsConverter openLyricsConverter = new OpenLyricsConverter();

    public SongStore getStore() {
        if (store == null) {
            store = new SongStore(Path.of(appContextService.getConfig().dataDir()));
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
