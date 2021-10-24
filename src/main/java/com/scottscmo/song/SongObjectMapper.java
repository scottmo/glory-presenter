package com.scottscmo.song;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SongObjectMapper {
    private static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper(new YAMLFactory());
    }

    public static Song deserialize(String serializedSong) {
        try {
            return mapper.readValue(serializedSong, Song.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Song getSong(String songName) {
        String songFileContent = getSongFileContent(songName);
        if (songFileContent != null) {
            return deserialize(songFileContent);
        }
        return null;
    }

    public static String getSongFileContent(String songName) {
        try {
            return Files.readString(Path.of("resources/songs/" + songName + ".yaml"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}