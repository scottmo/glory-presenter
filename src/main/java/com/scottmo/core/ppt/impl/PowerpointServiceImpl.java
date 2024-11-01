package com.scottmo.core.ppt.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.song.Song;

public class PowerpointServiceImpl implements PowerpointService {
    private BibleVerseHelper bibleVerseHelper = new BibleVerseHelper();
    private SongHelper songHelper = new SongHelper();

    @Override
    public void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath) throws IOException {
        TemplatingUtil.generateSlideShow(contents, tmplFilePath, outputFilePath);
    }


    @Override
    public void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException {
        List<Map<String, String>> slideContents = bibleVerseHelper.toSlideContents(bibleRefString);
        generate(slideContents, tmplFilePath, outputFilePath);
    }

    @Override
    public void generate(Integer songId, String tmplFilePath, String outputFilePath, int maxLines) throws IOException {
        List<Map<String, String>> slideContents = songHelper.toSlideContents(songId, maxLines);
        generate(slideContents, tmplFilePath, outputFilePath);
    }
    @Override
    public void generate(Song song, String tmplFilePath, String outputFilePath, int maxLines) throws IOException {
        List<Map<String, String>> slideContents = songHelper.toSlideContents(song, maxLines);
        generate(slideContents, tmplFilePath, outputFilePath);
    }

    @Override
    public void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException {
        TemplatingUtil.mergeSlideShows(filePaths, outputFilePath);
    }
}
