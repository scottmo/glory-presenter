package com.scottscmo.model.song;

import com.scottscmo.model.song.converters.KVMDConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public final class SongLoader {
    public static Song getSong(String dataPath, String titleSubstring) {
        File[] songFiles = new File(Path.of(dataPath).toString()).listFiles();
        if (songFiles != null) {
            Optional<String> songFile = Arrays.stream(songFiles)
                    .map(File::getName)
                    .sorted()
                    .filter(name -> name.toLowerCase().contains(titleSubstring))
                    .findFirst();
            if (songFile.isPresent()) {
                try {
                    String content = Files.readString(Path.of(dataPath, songFile.get()));
                    return KVMDConverter.parse(content);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load song by " + titleSubstring, e);
                }
            }
        }
        return null;
    }
}
