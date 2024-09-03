package com.scottmo.core.songs.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.scottmo.core.Service;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.Pair;

public interface SongService extends Service {

    List<Pair<Integer, String>> getAllSongDescriptors(List<String> locales);

    Song get(int songId);

    int store(Song song);

    boolean delete(int songId);

    String serializeToOpenLyrics(Song song);

    void importOpenLyricSong(File openLyricsFile) throws IOException;

    void importOpenLyricSong(String openLyricsXML) throws IOException;

}
