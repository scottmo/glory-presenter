package com.scottmo.services.songs;

import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.songs.openLyrics.OpenLyricsConverter;
import com.scottmo.services.songs.store.SongStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component("songService")
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
}
