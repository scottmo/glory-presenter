package com.scottmo.core.songs.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.scottmo.config.ConfigService;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.impl.openLyrics.OpenLyricsConverter;
import com.scottmo.core.songs.impl.store.SongStore;
import com.scottmo.shared.Pair;

public class SongServiceImpl implements SongService {
    private ConfigService configService = ConfigService.get();
    private SongStore store;
    private final OpenLyricsConverter openLyricsConverter = new OpenLyricsConverter();

    private SongStore getStore() {
        if (store == null) {
            store = new SongStore(Path.of(configService.getConfig().getDataDir()));
        }
        return store;
    }

    @Override
    public List<Pair<Integer, String>> getAllSongDescriptors(List<String> locales) {
        return getStore().getAllSongDescriptors(locales);
    }

    @Override
    public Song get(int songId) {
        return getStore().get(songId);
    }

    @Override
    public int store(Song song) {
        return getStore().store(song);
    }

    @Override
    public boolean delete(int songId) {
        return getStore().delete(songId);
    }

    @Override
    public String serialize(Song song) {
        return openLyricsConverter.serialize(song);
    }

    @Override
    public void importSong(File openLyricsFile) throws IOException {
        String openLyricsXML = Files.readString(openLyricsFile.toPath(), StandardCharsets.UTF_8);
        importSong(openLyricsXML);
    }

    @Override
    public void importSong(String openLyricsXML) throws IOException {
        Song song = openLyricsConverter.deserialize(openLyricsXML);
        store.store(song);
    }
}
