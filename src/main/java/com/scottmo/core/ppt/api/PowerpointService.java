package com.scottmo.core.ppt.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.scottmo.core.Service;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.Range;
import com.scottmo.shared.TextFormat;

public interface PowerpointService extends Service {
    void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath) throws IOException;

    void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException;

    void generate(Integer songId, String tmplFilePath, String outputFilePath, int maxLines) throws IOException;
    void generate(Song song, String tmplFilePath, String outputFilePath, int maxLines) throws IOException;

    void generateFromYamlConfigs(String yamlConfigs, String outputFilePath) throws IOException;

    void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException;

    void updateTextFormats(String filePath, String outputFilePath, Range range, Pattern textMatchPattern, TextFormat textFormats) throws IOException;

    void normalizeNewLines(String filePath, String outputFilePath) throws IOException;
}
