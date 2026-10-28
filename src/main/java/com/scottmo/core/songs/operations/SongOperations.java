package com.scottmo.core.songs.operations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.operations.ApplicationExecutionLock;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongTitle;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.shared.LocaleUtil;
import com.scottmo.shared.Pair;
import com.scottmo.shared.StringUtils;

/** Shared song workflows used by Swing and MCP. */
public final class SongOperations {
    private static final long MAX_OPEN_LYRICS_BYTES = 5L * 1024 * 1024;
    private static final int MAX_DELETE_COUNT = 100;

    private final SongService songs;
    private final PowerpointService powerpoints;
    private final Supplier<Config> configSupplier;
    private final ApplicationExecutionLock executionLock;

    public SongOperations(SongService songs, PowerpointService powerpoints, Supplier<Config> configSupplier,
        ApplicationExecutionLock executionLock) {
        this.songs = songs;
        this.powerpoints = powerpoints;
        this.configSupplier = configSupplier;
        this.executionLock = executionLock;
    }

    public static SongOperations forApplication() {
        SongService songs = ServiceProvider.get(SongService.class).get();
        PowerpointService powerpoints = ServiceProvider.get(PowerpointService.class).get();
        return new SongOperations(songs, powerpoints, ConfigService.get()::getConfig,
            ApplicationExecutionLock.shared());
    }

    public List<SongSummary> list(String query, List<String> locales) {
        return executionLock.call(() -> {
            List<String> selectedLocales = locales == null ? configuredLocales() : normalizeLocales(locales);
            String search = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            return songs.getAllSongDescriptors(selectedLocales).stream()
                .filter(pair -> search.isEmpty() || pair.value().toLowerCase(Locale.ROOT).contains(search))
                .map(pair -> new SongSummary(pair.key(), pair.value()))
                .sorted((left, right) -> {
                    int byName = left.descriptor().compareToIgnoreCase(right.descriptor());
                    return byName == 0 ? Integer.compare(left.id(), right.id()) : byName;
                })
                .toList();
        });
    }

    public Song get(int id) {
        return executionLock.call(() -> {
            requireExistingSong(id);
            return songs.get(id);
        });
    }

    public int create(Song candidate) {
        return executionLock.call(() -> {
            Song song = normalizeSong(candidate, -1);
            int id = songs.store(song);
            return id;
        });
    }

    public int save(Song candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Song object is required");
        }
        return candidate.getId() < 0 ? create(candidate) : update(candidate.getId(), candidate);
    }

    public int update(int id, Song candidate) {
        return executionLock.call(() -> {
            requireExistingSong(id);
            Song song = normalizeSong(candidate, id);
            return songs.store(song);
        });
    }

    public int duplicate(int id) {
        return executionLock.call(() -> {
            requireExistingSong(id);
            return songs.store(duplicateDraft(id));
        });
    }

    public Song duplicateDraft(int id) {
        return executionLock.call(() -> {
            requireExistingSong(id);
            return copySong(songs.get(id), -1);
        });
    }

    public boolean delete(List<Integer> ids) {
        return executionLock.call(() -> {
            if (ids == null || ids.isEmpty()) {
                throw new IllegalArgumentException("At least one song id is required");
            }
            if (ids.size() > MAX_DELETE_COUNT) {
                throw new IllegalArgumentException("At most " + MAX_DELETE_COUNT + " songs can be deleted at once");
            }
            Set<Integer> unique = new HashSet<>(ids);
            if (unique.size() != ids.size()) {
                throw new IllegalArgumentException("Song ids must be unique");
            }
            for (Integer id : ids) {
                if (id == null) {
                    throw new IllegalArgumentException("Song ids must not contain null");
                }
                requireExistingSong(id);
            }
            return songs.delete(ids);
        });
    }

    public int importOpenLyrics(String xml) throws IOException {
        return executionLock.io(() -> {
            if (xml == null || xml.isBlank()) {
                throw new IllegalArgumentException("OpenLyrics XML is required");
            }
            if (xml.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_OPEN_LYRICS_BYTES) {
                throw new IllegalArgumentException("OpenLyrics XML exceeds the 5 MB limit");
            }
            int id = songs.importSongAndGetId(xml);
            return id;
        });
    }

    public int importOpenLyrics(Path file) throws IOException {
        return executionLock.io(() -> {
            if (file == null || !Files.isRegularFile(file)) {
                throw new IllegalArgumentException("OpenLyrics file does not exist: " + file);
            }
            if (Files.size(file) > MAX_OPEN_LYRICS_BYTES) {
                throw new IllegalArgumentException("OpenLyrics file exceeds the 5 MB limit");
            }
            return importOpenLyrics(Files.readString(file));
        });
    }

    public ExportResult exportOpenLyrics(int id, Path outputFile) throws IOException {
        return executionLock.io(() -> {
            Song song = requiredSong(id);
            String xml = songs.serialize(song);
            Path written = writeOptionalOutput(xml, outputFile == null ? null : resolveOutput(outputFile));
            return new ExportResult(xml, written == null ? null : written.toString());
        });
    }

    public ExportResult exportYaml(int id, Path outputFile) throws IOException {
        return executionLock.io(() -> {
            Song song = requiredSong(id);
            String yaml = songs.serializeToYaml(song);
            Path written = writeOptionalOutput(yaml, outputFile == null ? null : resolveOutput(outputFile));
            return new ExportResult(yaml, written == null ? null : written.toString());
        });
    }

    public String defaultOpenLyricsPath(int id) {
        return executionLock.call(() -> defaultOutputPath(requiredSong(id), ".xml").toString());
    }

    public String defaultYamlPath(int id) {
        return executionLock.call(() -> outputDirectory().resolve(songs.getExportFilename(requiredSong(id), configuredLocales())).toString());
    }

    public String generatePowerpoint(int id, int linesPerSlide, String template, Path outputFile) throws IOException {
        return executionLock.io(() -> {
            if (linesPerSlide < 1 || linesPerSlide > 10) {
                throw new IllegalArgumentException("linesPerSlide must be between 1 and 10");
            }
            Song song = requiredSong(id);
            Path templatePath = resolveTemplate(template);
            Path destination = outputFile == null ? defaultOutputPath(song, ".pptx") : resolveOutput(outputFile);
            Files.createDirectories(destination.toAbsolutePath().getParent());
            powerpoints.generate(song, templatePath.toString(), destination.toString(), linesPerSlide);
            return destination.toString();
        });
    }

    public List<String> generateVerseOrder(List<SongVerse> verses) {
        return SongVerseOrder.generate(verses);
    }

    private Song requiredSong(int id) {
        requireExistingSong(id);
        return songs.get(id);
    }

    private void requireExistingSong(int id) {
        if (id < 0 || !songs.exists(id)) {
            throw new NoSuchElementException("Song " + id + " does not exist");
        }
    }

    private Song normalizeSong(Song candidate, int id) {
        if (candidate == null) {
            throw new IllegalArgumentException("Song object is required");
        }
        List<SongTitle> sourceTitles = candidate.getTitles();
        List<SongVerse> sourceVerses = candidate.getVerses();
        if (sourceTitles == null || sourceVerses == null) {
            throw new IllegalArgumentException("Song titles and verses must be arrays");
        }
        List<SongTitle> titles = new ArrayList<>();
        Set<String> titleLocales = new HashSet<>();
        for (SongTitle title : sourceTitles) {
            if (title == null || title.getLocale() == null || title.getLocale().isBlank() || title.getText() == null) {
                throw new IllegalArgumentException("Every song title needs a locale and text");
            }
            String locale = LocaleUtil.normalize(title.getLocale());
            if (!titleLocales.add(locale)) {
                throw new IllegalArgumentException("Song title locales must be unique: " + locale);
            }
            titles.add(new SongTitle(title.getText(), locale));
        }
        List<SongVerse> verses = new ArrayList<>();
        Set<String> verseKeys = new HashSet<>();
        Set<String> verseNames = new HashSet<>();
        for (SongVerse verse : sourceVerses) {
            if (verse == null || verse.getName() == null || verse.getName().isBlank()
                || verse.getLocale() == null || verse.getLocale().isBlank() || verse.getText() == null) {
                throw new IllegalArgumentException("Every song verse needs a name, locale, and text");
            }
            String name = verse.getName().trim();
            String locale = LocaleUtil.normalize(verse.getLocale());
            if (!verseKeys.add(name + "\u0000" + locale)) {
                throw new IllegalArgumentException("Song verse names must be unique per locale: " + name + " (" + locale + ")");
            }
            verseNames.add(name);
            verses.add(new SongVerse(name, verse.getText(), locale));
        }
        List<String> authors = candidate.getAuthors();
        if (authors == null || authors.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Song authors must be an array of strings");
        }
        List<String> order = candidate.getVerseOrder();
        if (order == null || order.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Song verseOrder must be an array of nonblank strings");
        }
        for (String name : order) {
            if (!verseNames.contains(name)) {
                throw new IllegalArgumentException("Song verseOrder references missing verse: " + name);
            }
        }
        boolean hasTitle = titles.stream().anyMatch(title -> !title.getText().isBlank());
        boolean hasLyrics = verses.stream().anyMatch(verse -> !verse.getText().isBlank());
        if (!hasTitle && !hasLyrics) {
            throw new IllegalArgumentException("Song must contain a title or lyric text");
        }
        Config config = configSupplier.get();
        String defaultLocale = config.getLocales() == null || config.getLocales().isEmpty()
            ? LocaleUtil.DEFAULT_LOCALE
            : config.getLocales().get(0);
        return new Song(id)
            .setDefaultLocale(defaultLocale)
            .setTitles(titles)
            .setAuthors(new ArrayList<>(authors))
            .setPublisher(candidate.getPublisher())
            .setCopyright(candidate.getCopyright())
            .setSongBook(candidate.getSongBook())
            .setEntry(candidate.getEntry())
            .setComments(candidate.getComments())
            .setVerseOrder(new ArrayList<>(order))
            .setVerses(verses);
    }

    private Song copySong(Song source, int id) {
        return new Song(id)
            .setDefaultLocale(configuredLocales().get(0))
            .setTitles(source.getTitles().stream().map(title -> new SongTitle(title.getText(), title.getLocale())).toList())
            .setAuthors(new ArrayList<>(source.getAuthors()))
            .setPublisher(source.getPublisher())
            .setCopyright(source.getCopyright())
            .setSongBook(source.getSongBook())
            .setEntry(source.getEntry())
            .setComments(source.getComments())
            .setVerseOrder(new ArrayList<>(source.getVerseOrder()))
            .setVerses(source.getVerses().stream()
                .map(verse -> new SongVerse(verse.getName(), verse.getText(), verse.getLocale())).toList());
    }

    private List<String> configuredLocales() {
        List<String> locales = configSupplier.get().getLocales();
        if (locales == null || locales.isEmpty()) {
            return List.of(LocaleUtil.DEFAULT_LOCALE);
        }
        return normalizeLocales(locales);
    }

    private static List<String> normalizeLocales(List<String> locales) {
        return locales.stream().map(locale -> {
            if (locale == null || locale.isBlank()) {
                throw new IllegalArgumentException("Locales must not contain blank values");
            }
            return LocaleUtil.normalize(locale);
        }).distinct().toList();
    }

    private Path resolveTemplate(String selectedTemplate) {
        Config config = configSupplier.get();
        Path template;
        if (selectedTemplate == null || selectedTemplate.isBlank()) {
            String configured = config.getDefaultTemplates() == null ? null : config.getDefaultTemplates().get("song");
            String defaultTemplate = configured != null && !configured.isBlank() ? configured
                : config.getTemplatePaths().stream()
                .filter(path -> path.toLowerCase(Locale.ROOT).contains("song"))
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No song PowerPoint template is configured"));
            template = Path.of(config.getDataDir(), Config.TEMPLATE_DIR, defaultTemplate);
        } else {
            Path requested = Path.of(selectedTemplate);
            template = requested.isAbsolute()
                ? requested
                : Path.of(config.getDataDir(), Config.TEMPLATE_DIR).resolve(requested);
        }
        if (!Files.isRegularFile(template)) {
            throw new IllegalArgumentException("PowerPoint template does not exist: " + template);
        }
        return template.toAbsolutePath().normalize();
    }

    private Path outputDirectory() {
        String configured = configSupplier.get().getOutputDir();
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Application outputDir is not configured");
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private Path defaultOutputPath(Song song, String extension) {
        String title = song.getTitle();
        String base = title == null || title.isBlank() ? "song_" + song.getId() : title;
        String safe = StringUtils.sanitizeFilename(base).trim();
        if (safe.isBlank()) {
            safe = "song_" + song.getId();
        }
        return outputDirectory().resolve(safe + extension);
    }

    private Path resolveOutput(Path path) {
        return (path.isAbsolute() ? path : outputDirectory().resolve(path)).toAbsolutePath().normalize();
    }

    private Path writeOptionalOutput(String content, Path outputFile) throws IOException {
        if (outputFile == null) {
            return null;
        }
        Path absolute = outputFile.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(absolute, content, java.nio.charset.StandardCharsets.UTF_8);
        return absolute;
    }

    public record SongSummary(int id, String descriptor) {}
    public record ExportResult(String content, String path) {}
}
