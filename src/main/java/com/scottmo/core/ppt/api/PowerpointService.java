package com.scottmo.core.ppt.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.scottmo.core.Service;
import com.scottmo.core.songs.api.song.Song;

public interface PowerpointService extends Service {
    public void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath) throws IOException;

    public void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException;

    public void generate(Integer songId, String tmplFilePath, String outputFilePath, int maxLines) throws IOException;
    public void generate(Song song, String tmplFilePath, String outputFilePath, int maxLines) throws IOException;

    public void generateFromYamlConfigs(String yamlConfigs, String outputFilePath) throws IOException;

    public void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException;
}
