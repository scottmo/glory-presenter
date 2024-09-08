package com.scottmo.core.ppt.api;

import java.io.IOException;
import java.util.List;

import com.scottmo.core.Service;
import com.scottmo.core.songs.api.song.Song;

public interface SongSlidesGenerator extends Service {

    // TODO: determine hasStartSlide and hasEndSlide from templatefile
    void generate(Song song, String tmplFilePath, String outputFilePath, List<String> locales,
            int maxLines) throws IOException;

    void generate(Song song, String tmplFilePath, String outputFilePath, List<String> locales,
            int maxLines, boolean hasStartSlide, boolean hasEndSlide) throws IOException;

}
