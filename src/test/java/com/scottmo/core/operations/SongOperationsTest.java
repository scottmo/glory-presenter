package com.scottmo.core.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.scottmo.config.Config;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.ppt.impl.PowerpointServiceImpl;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongTitle;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.core.songs.impl.SongServiceImpl;
import com.scottmo.core.songs.operations.SongOperations;

class SongOperationsTest {
    @TempDir
    Path tempDir;

    private SongServiceImpl songs;

    @AfterEach
    void closeStore() {
        if (songs != null) {
            songs.close();
        }
    }

    @Test
    void supportsCreateUpdateDuplicateSearchAndDeleteWithStableIds() {
        SongOperations operations = operations(tempDir.resolve("data"), config(tempDir.resolve("data")));

        int id = operations.create(song("Amazing Grace", "en_us", "v1", "Amazing grace"));
        Song created = operations.get(id);
        assertEquals(id, created.getId());
        assertEquals("Amazing Grace", created.getTitle("en_us"));
        assertEquals(List.of(new SongOperations.SongSummary(id, "Amazing Grace")), operations.list("amazing", List.of("en_us")));

        Song changed = song("Grace", "en_us", "v1", "How sweet");
        changed.setPublisher("John Newton");
        assertEquals(id, operations.update(id, changed));
        assertEquals("Grace", operations.get(id).getTitle("en_us"));
        assertEquals("John Newton", operations.get(id).getPublisher());

        int duplicateId = operations.duplicate(id);
        assertTrue(duplicateId != id);
        assertEquals("Grace", operations.get(duplicateId).getTitle("en_us"));
        assertEquals(2, operations.list("grace", List.of("en_us")).size());

        assertTrue(operations.delete(List.of(id, duplicateId)));
        assertTrue(operations.list(null, List.of("en_us")).isEmpty());
        assertThrows(java.util.NoSuchElementException.class, () -> operations.get(id));
    }

    @Test
    void acceptsTitleOnlySongAndRejectsInvalidVerseOrderAndDuplicateVerseLocale() {
        SongOperations operations = operations(tempDir.resolve("data"), config(tempDir.resolve("data")));
        Song titleOnly = new Song(-1).setTitles(List.of(new SongTitle("Title only", "en_us")));
        int id = operations.create(titleOnly);
        assertEquals("Title only", operations.get(id).getTitle("en_us"));

        Song missingOrderTarget = song("Invalid", "en_us", "v2", "Text");
        missingOrderTarget.setVerseOrder(List.of("v1"));
        assertThrows(IllegalArgumentException.class, () -> operations.create(missingOrderTarget));

        Song duplicateVerse = song("Invalid", "en_us", "v1", "Text");
        duplicateVerse.setVerses(List.of(new SongVerse("v1", "one", "en_us"),
            new SongVerse("v1", "two", "en_us")));
        assertThrows(IllegalArgumentException.class, () -> operations.create(duplicateVerse));

        Song nullAuthors = song("Invalid", "en_us", "v1", "Text").setAuthors(null);
        assertThrows(IllegalArgumentException.class, () -> operations.create(nullAuthors));
    }

    @Test
    void importsFirstThenExportsContentAndOptionalFiles() throws Exception {
        SongOperations operations = operations(tempDir.resolve("data"), config(tempDir.resolve("data")));
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <song xmlns="http://openlyrics.info/namespace/2009/song">
              <properties><titles><title>Amazing Grace</title></titles><verseOrder>v1</verseOrder></properties>
              <lyrics><verse name="v1"><lines>Amazing grace</lines></verse></lyrics>
            </song>
            """;

        int id = operations.importOpenLyrics(xml);
        assertEquals("Amazing Grace", operations.get(id).getTitle("en_us"));
        SongOperations.ExportResult xmlResult = operations.exportOpenLyrics(id, tempDir.resolve("exports/song.xml"));
        SongOperations.ExportResult yamlResult = operations.exportYaml(id, tempDir.resolve("exports/song.yaml"));
        assertTrue(xmlResult.content().contains("Amazing Grace"));
        assertTrue(yamlResult.content().contains("Amazing Grace"));
        assertTrue(Files.isRegularFile(Path.of(xmlResult.path())));
        assertTrue(Files.isRegularFile(Path.of(yamlResult.path())));
        assertFalse(operations.exportYaml(id, null).content().isBlank());
    }

    @Test
    void generatesSlidesWithConfiguredNestedDefaultTemplate() throws Exception {
        Path dataDir = tempDir.resolve("data");
        Path outputDir = tempDir.resolve("output");
        Path template = dataDir.resolve("templates/worship/ordinary-name.pptx");
        Files.createDirectories(template.getParent());
        Files.copy(Path.of("sample-templates", "template-song.pptx"), template);
        Config config = config(dataDir);
        config.setOutputDir(outputDir.toString());
        config.setDefaultTemplates(Map.of("song", "worship/ordinary-name.pptx"));
        SongOperations operations = operations(dataDir, config);
        int id = operations.create(song("Amazing Grace", "en_us", "v1", "Amazing grace"));

        String output = operations.generatePowerpoint(id, 3, null, tempDir.resolve("slides/song.pptx"));

        Path generated = Path.of(output);
        assertTrue(Files.isRegularFile(generated));
        assertTrue(Files.size(generated) > 0);
        try (var ppt = new org.apache.poi.xslf.usermodel.XMLSlideShow(Files.newInputStream(generated))) {
            assertFalse(ppt.getSlides().isEmpty());
        }
    }

    private SongOperations operations(Path dataDir, Config config) {
        songs = new SongServiceImpl(dataDir);
        PowerpointService powerpoints = new PowerpointServiceImpl(config::getLocales, songs);
        return new SongOperations(songs, powerpoints, () -> config, ApplicationExecutionLock.shared());
    }

    private Config config(Path dataDir) {
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(tempDir.resolve("output").toString());
        config.setLocales(List.of("en_us"));
        return config;
    }

    private static Song song(String title, String locale, String verseName, String lyrics) {
        return new Song(-1)
            .setTitles(List.of(new SongTitle(title, locale)))
            .setVerses(List.of(new SongVerse(verseName, lyrics, locale)))
            .setVerseOrder(List.of(verseName));
    }
}
