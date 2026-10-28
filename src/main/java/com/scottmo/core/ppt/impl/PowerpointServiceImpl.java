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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PowerpointServiceImpl implements PowerpointService {

    private final ConfigService configService;
    private final Path configuredTemplateRoot;
    private final Map<String, String> configuredDefaultTemplates;
    private final BibleVerseHelper bibleVerseHelper;
    private final SongHelper songHelper;

    private final YAMLMapper yamlMapper = new YAMLMapper();

    private final Logger logger = Logger.getLogger(PowerpointServiceImpl.class.getName());

    public PowerpointServiceImpl() {
        this(ConfigService.get(), null, null);
    }

    /** Injection seam for isolated template generation and deterministic tests. */
    public PowerpointServiceImpl(Path templateRoot, Map<String, String> defaultTemplates) {
        this(null, Objects.requireNonNull(templateRoot, "templateRoot").toAbsolutePath().normalize(),
            Map.copyOf(defaultTemplates), () -> List.of(), null);
    }

    public PowerpointServiceImpl(Supplier<List<String>> locales, com.scottmo.core.songs.api.SongService songs) {
        this(ConfigService.get(), null, null, locales, songs);
    }

    private PowerpointServiceImpl(ConfigService configService, Path templateRoot, Map<String, String> defaultTemplates) {
        this(configService, templateRoot, defaultTemplates,
            () -> ConfigService.get().getConfig().getLocales(),
            null);
    }

    private PowerpointServiceImpl(ConfigService configService, Path templateRoot, Map<String, String> defaultTemplates,
            Supplier<List<String>> locales, com.scottmo.core.songs.api.SongService songs) {
        this.configService = configService;
        this.configuredTemplateRoot = templateRoot;
        this.configuredDefaultTemplates = defaultTemplates;
        this.bibleVerseHelper = new BibleVerseHelper();
        var actualSongService = songs;
        if (actualSongService == null && templateRoot == null) {
            actualSongService = com.scottmo.core.ServiceProvider.get(com.scottmo.core.songs.api.SongService.class).get();
        }
        this.songHelper = new SongHelper(locales, actualSongService);
    }

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
        if (maxLines < 1) {
            throw new IllegalArgumentException("linesPerSlide must be at least 1");
        }
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
        List<PowerpointConfig> configs;
        try {
            configs = yamlMapper.readValue(yamlConfigs,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, PowerpointConfig.class));
        } catch (IOException e) {
            throw new IOException("Unable to parse program YAML configuration: " + e.getMessage(), e);
        }

        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("Program YAML must contain at least one slide set");
        }
        if (configs.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Program YAML contains an empty slide set");
        }

        Map<String, String> defaultTemplates = configuredDefaultTemplates != null
            ? configuredDefaultTemplates
            : configService.getConfig().getDefaultTemplates();

        List<Path> tempFiles = new ArrayList<>();
        try {
            // Generate individual PPT files
            List<String> tempFilePaths = new ArrayList<>();
            for (int i = 0; i < configs.size(); i++) {
                PowerpointConfig config = configs.get(i);
                PowerpointConfig.Type type = config.getType() == null ? PowerpointConfig.Type.DEFAULT : config.getType();
                Path temporaryFile = Files.createTempFile("glory-presenter-" + type.name().toLowerCase() + "-", ".pptx");
                tempFiles.add(temporaryFile);
                String tempFilePath = temporaryFile.toString();

                String templatePath;
                if (config.getTemplate() != null) {
                    templatePath = resolveTemplate(config.getTemplate());
                } else if (defaultTemplates.containsKey(type.toValue())) {
                    templatePath = resolveTemplate(defaultTemplates.get(type.toValue()));
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
                        if (config.getContent() == null || config.getContent().isBlank()) {
                            throw new IllegalArgumentException("Song slide set " + i + " requires content with songId and linesPerSlide");
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> songConfig = yamlMapper.readValue(config.getContent(), Map.class);
                        Integer songId = songConfig.get("songId");
                        Integer linesPerSlide = songConfig.get("linesPerSlide");
                        if (songId == null || linesPerSlide == null) {
                            throw new IllegalArgumentException("Song slide set " + i + " requires songId and linesPerSlide");
                        }
                        if (linesPerSlide < 1) {
                            throw new IllegalArgumentException("Song slide set " + i + " linesPerSlide must be at least 1");
                        }
                        generate(songId, templatePath, tempFilePath, linesPerSlide);
                        break;
                    case BIBLE:
                        String verses = config.getContent();
                        if (verses == null || verses.isBlank()) {
                            throw new IllegalArgumentException("Bible slide set " + i + " requires a Bible reference in content");
                        }
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
                tempFilePaths.add(tempFilePath);
            }
            logger.info("Merging temporary powerpoints into " + outputFilePath);
            if (tempFilePaths.isEmpty()) {
                throw new IllegalArgumentException("Program YAML must contain at least one slide set");
            }
            mergeSlideShows(tempFilePaths, outputFilePath);
        } finally {
            cleanupTemporaryFiles(tempFiles);
        }
    }

    private String resolveTemplate(String name) throws IOException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        Path templateRoot = configuredTemplateRoot != null
            ? configuredTemplateRoot
            : Path.of(configService.getConfig().getDataDir(), com.scottmo.config.Config.TEMPLATE_DIR);
        Path root = templateRoot.toRealPath();
        Path supplied = Path.of(name);
        if (supplied.isAbsolute()) {
            throw new IllegalArgumentException("Template path must be relative to the templates directory: " + name);
        }
        Path candidate = root.resolve(supplied).normalize();
        if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("Template path escapes the templates directory: " + name);
        }
        Path realCandidate = candidate.toRealPath();
        if (!realCandidate.startsWith(root) || !Files.isRegularFile(realCandidate)) {
            throw new IllegalArgumentException("Template is not a file inside the templates directory: " + name);
        }
        return realCandidate.toString();
    }

    @Override
    public void updateTextFormats(String filePath, String outputFilePath, Range range, Pattern textMatchPattern, TextFormat textFormats) throws IOException {
        TemplatingUtil.loadSlideShow(filePath, ppt -> {
            List<XSLFSlide> slides = range.endIndex() == 0
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
                TemplatingUtil.convertNewlinesIntoParagraphs(slide);
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
            if (formats.getFontColorObject() != null) {
                textRun.setFontColor(formats.getFontColorObject());
            }
            textRun.setBold(formats.isBold());
            textRun.setItalic(formats.isItalic());
            textRun.setUnderlined(formats.isUnderlined());
            textRun.setStrikethrough(formats.isStrikethrough());
        }
    }

    private void cleanupTemporaryFiles(List<Path> tempFiles) {
        for (Path path : tempFiles) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                logger.error("Unable to delete temporary file: " + path, e);
            }
        }
    }
}
