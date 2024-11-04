package com.scottmo.core.ppt.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ppt.api.PowerpointConfig;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.song.Song;

public class PowerpointServiceImpl implements PowerpointService {

    private ConfigService configService = ConfigService.get();
    private BibleVerseHelper bibleVerseHelper = new BibleVerseHelper();
    private SongHelper songHelper = new SongHelper();

    private YAMLMapper yamlMapper = new YAMLMapper();

    private Logger logger = Logger.getLogger(PowerpointServiceImpl.class.getName());

    @Override
    public void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath)
            throws IOException {
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

    @Override
    public void generateFromYamlConfigs(String yamlConfigs) throws IOException {
        List<PowerpointConfig> configs = yamlMapper.readValue(yamlConfigs,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, PowerpointConfig.class));

        // Generate individual PPT files
        List<String> tempFiles = new ArrayList<>();
        for (int i = 0; i < configs.size(); i++) {
            PowerpointConfig config = configs.get(i);
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + config.type() + i + ".pptx";
            String templatePath = configService.getRelativePath(config.template());
            String type = config.type() == null ? "default" : config.type().toLowerCase();

            logger.info("Generating temporary powerpoint:"
                + "\n- type: " + type
                + "\n- template: " + templatePath
                + "\n- out: " + tempFilePath
                + "\n- content:\n" + config.content());

            switch (type) {
                case "song":
                    int songId = Integer.parseInt(config.content());
                    generate(songId, templatePath, tempFilePath, 2);
                    break;
                case "bible":
                    String verses = config.content();
                    generate(verses, templatePath, tempFilePath);
                    break;
                default:
                    List<Map<String, String>> values = yamlMapper.readValue(config.content(),
                            yamlMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    generate(values, templatePath, tempFilePath);
                    break;
            }
            tempFiles.add(tempFilePath);
        }
        String outputPath = configService.getOutputPath("slidesShow.ppt");
        logger.info("Merging temporary powerpoints into " + outputPath);
        mergeSlideShows(tempFiles, outputPath);
    }
}
