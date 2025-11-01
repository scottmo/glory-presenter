package com.scottmo.core.ppt.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ppt.api.PowerpointConfig;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.Range;
import com.scottmo.shared.TextFormat;
import org.apache.log4j.Logger;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PowerpointServiceImpl implements PowerpointService {

    private final ConfigService configService = ConfigService.get();
    private final BibleVerseHelper bibleVerseHelper = new BibleVerseHelper();
    private final SongHelper songHelper = new SongHelper();

    private final YAMLMapper yamlMapper = new YAMLMapper();

    private final Logger logger = Logger.getLogger(PowerpointServiceImpl.class.getName());

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
    public void generateFromYamlConfigs(String yamlConfigs, String outputFilePath) throws IOException {
        List<PowerpointConfig> configs = yamlMapper.readValue(yamlConfigs,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, PowerpointConfig.class));

        Map<String, String> defaultTemplates = configService.getConfig().getDefaultTemplates();

        try {
            // Generate individual PPT files
            List<String> tempFiles = new ArrayList<>();
            for (int i = 0; i < configs.size(); i++) {
                PowerpointConfig config = configs.get(i);
                PowerpointConfig.Type type = config.getType() == null ? PowerpointConfig.Type.DEFAULT : config.getType();
                String tempFilePath = getTemporaryFilePath(type.name() + i);

                String templatePath;
                if (config.getTemplate() != null) {
                    templatePath = configService.getPowerpointTemplate(config.getTemplate());
                } else if (defaultTemplates.containsKey(type.toValue())) {
                    templatePath = configService.getPowerpointTemplate(defaultTemplates.get(type.toValue()));
                } else {
                    throw new IllegalArgumentException("No defined template for slide set " + i + "!");
                }

                logger.info("Generating temporary powerpoint:"
                    + "\n- type: " + type
                    + "\n- template: " + templatePath
                    + "\n- out: " + tempFilePath
                    + "\n- content:\n" + config.getContent());

                switch (type) {
                    case SONG:
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> songConfig = yamlMapper.readValue(config.getContent(), Map.class);
                        generate(songConfig.get("songId"), templatePath, tempFilePath, songConfig.get("linesPerSlide"));
                        break;
                    case BIBLE:
                        String verses = config.getContent();
                        generate(verses, templatePath, tempFilePath);
                        break;
                    default:
                        List<Map<String, String>> values = config.getContent() == null
                            ? List.of()
                            : yamlMapper.readValue(config.getContent(),
                                yamlMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        generate(values, templatePath, tempFilePath);
                        break;
                }
                tempFiles.add(tempFilePath);
            }
            logger.info("Merging temporary powerpoints into " + outputFilePath);
            mergeSlideShows(tempFiles, outputFilePath);
        } finally {
            cleanupTemporaryFiles();
        }
    }

    @Override
    public void updateTextFormats(String filePath, String outputFilePath, Range range, Pattern textMatchPattern, TextFormat textFormats) throws IOException {
        TemplatingUtil.loadSlideShow(filePath, ppt -> {
            List<XSLFSlide> slides = range.endIndex() == -1
                ? ppt.getSlides()
                : ppt.getSlides().subList(range.startIndex(), range.endIndex());
            for (var slide : slides) {
                for (var shape: slide.getShapes()) {
                    if (!(shape instanceof XSLFTextShape)) {
                        continue;
                    }
                    for (var pp : ((XSLFTextShape) shape).getTextParagraphs()) {
                        for (var textRun : pp.getTextRuns()) {
                            String text = textRun.getRawText();
                            if (textMatchPattern.matcher(text).find()) {
                                applyTextFormats(textRun, textFormats);
                            }
                        }
                    }
                }
            }

            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
        });
    }

    @Override
    public void normalizeNewLines(String filePath, String outputFilePath) throws IOException {
        TemplatingUtil.loadSlideShow(filePath, ppt -> {
            for (var slide : ppt.getSlides()) {
                // convert \n to new pp
                slide.getShapes().stream()
                    .filter(s -> s instanceof XSLFTextShape)
                    .forEach(s -> {
                        XSLFTextShape shape = (XSLFTextShape) s;
                        TemplatingUtil.convertNewlinesIntoParagraphs(shape);
                    });
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
        });
    }

    private void applyTextFormats(XSLFTextRun textRun, TextFormat formats) {
        if (formats != null) {
            if (formats.getFontFamily() != null) {
                textRun.setFontFamily(formats.getFontFamily());
            }
            if (formats.getFontSize() != null) {
                textRun.setFontSize(formats.getFontSize());
            }
            if (formats.getFontColor() != null) {
                textRun.setFontColor(formats.getFontColor());
            }
            textRun.setBold(formats.isBold());
            textRun.setItalic(formats.isItalic());
            textRun.setUnderlined(formats.isUnderlined());
            textRun.setStrikethrough(formats.isStrikethrough());
        }
    }

    private final List<String> tempFiles = new ArrayList<>();
    private String getTemporaryFilePath(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String path = System.getProperty("java.io.tmpdir") + prefix + "_" + timestamp + ".pptx";
        tempFiles.add(path);
        return path;
    }
    private void cleanupTemporaryFiles() {
        for (String path : tempFiles) {
            File file = new File(path);
            if (!file.delete()) {
                logger.error("Unable to delete temporary file: " + path);
            }
        }
        tempFiles.clear();
    }
}
