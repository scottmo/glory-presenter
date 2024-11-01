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
        generate(contents, tmplFilePath, outputFilePath);
    }
    @Override
    public void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath,
            boolean hasStartSlide, boolean hasEndSlide) throws IOException {
        TemplatingUtil.generateSlideShow(contents, tmplFilePath, outputFilePath, hasStartSlide, hasEndSlide);
    }

    @Override
    public void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException {
        generate(bibleRefString, tmplFilePath, outputFilePath, true, false);
    }
    @Override
    public void generate(String bibleRefString, String tmplFilePath, String outputFilePath,
            boolean hasStartSlide, boolean hasEndSlide) throws IOException {
        List<Map<String, String>> slideContents = bibleVerseHelper.toSlideContents(bibleRefString, hasStartSlide, hasEndSlide);
        generate(slideContents, tmplFilePath, outputFilePath, hasStartSlide, hasEndSlide);
    }

    @Override
    public void generate(Song song, String tmplFilePath, String outputFilePath, List<String> locales,
            int maxLines) throws IOException {
        generate(song, tmplFilePath, outputFilePath, locales, maxLines, true, false);
    }
    @Override
    public void generate(Song song, String tmplFilePath, String outputFilePath, List<String> locales,
            int maxLines, boolean hasStartSlide, boolean hasEndSlide) throws IOException {
        List<Map<String, String>> slideContents = songHelper.toSlideContents(song, locales, maxLines, hasStartSlide, hasEndSlide);
        generate(slideContents, tmplFilePath, outputFilePath, hasStartSlide, hasEndSlide);
    }

    @Override
    public void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException {
        TemplatingUtil.mergeSlideShows(filePaths, outputFilePath);
    }
}
