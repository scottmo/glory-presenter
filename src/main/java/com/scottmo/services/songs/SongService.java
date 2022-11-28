package com.scottmo.services.songs;

import com.scottmo.services.Service;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.songs.openLyrics.OpenLyricsConverter;
import com.scottmo.services.songs.store.SongStore;

import java.nio.file.Path;

public class SongService implements Service {
    private final SongStore store = new SongStore(Path.of(ServiceSupplier.getAppContext().getConfig().dataDir()));
    private final OpenLyricsConverter openLyricsConverter = new OpenLyricsConverter();

    public SongStore getStore() {
        return store;
    }

    public OpenLyricsConverter getOpenLyricsConverter() {
        return openLyricsConverter;
    }
}
