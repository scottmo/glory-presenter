package com.scottmo.core.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import com.scottmo.config.Config;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleReference.BibleReference;
import com.scottmo.core.bible.impl.BibleServiceImpl;
import com.scottmo.core.operations.ApplicationExecutionLock;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.ppt.impl.PowerpointServiceImpl;

class BibleOperationsTest {
    private static final String JOHN_OSIS = """
        <osis>
          <osisText osisIDWork="NIV">
            <div osisID="John">
              <chapter osisID="John.1">
                <verse osisID="John.1.1">In the beginning was the Word.</verse>
                <verse osisID="John.1.2">He was with God in the beginning.</verse>
              </chapter>
            </div>
          </osisText>
        </osis>
        """;

    @TempDir
    Path tempDir;
    private final List<BibleServiceImpl> servicesToClose = new java.util.ArrayList<>();

    @AfterEach
    void closeBibleStores() throws Exception {
        for (BibleServiceImpl service : servicesToClose) {
            service.close();
        }
    }

    @Test
    void freshDatabaseListsNoVersionsAndNoBookNames() {
        BibleService service = newBibleService(tempDir.resolve("data"));

        assertTrue(service.getAvailableVersions().isEmpty());
        assertTrue(service.getBookNames("john").isEmpty());
    }

    @Test
    void importFirstThenQueryAndResolveCanonicalBookNames() throws Exception {
        Path dataDir = tempDir.resolve("data");
        BibleService service = newBibleService(dataDir);
        Path osisFile = writeOsis("niv.xml", JOHN_OSIS);
        BibleOperations operations = operations(service, new NoopPowerpoint(), config(dataDir));

        BibleOperations.ImportResult result = operations.importOsis(List.of(osisFile.toString()));
        assertEquals(List.of(osisFile.toString()), result.imported());
        assertTrue(result.failures().isEmpty());
        assertEquals(List.of("niv"), operations.listVersions());
        assertEquals(Map.of("niv", "john"), operations.getBookNames("john"));

        Map<String, List<BibleVerse>> verses = operations.getVerses("John 1:1-2", null);
        assertEquals(List.of("In the beginning was the Word.", "He was with God in the beginning."),
            verses.get("niv").stream().map(BibleVerse::text).toList());
    }

    @Test
    void failedOsisFilesAreReportedAlongsideSuccessfulImports() throws Exception {
        BibleService service = newBibleService(tempDir.resolve("data"));
        Path validFile = writeOsis("valid.xml", JOHN_OSIS);
        Path invalidFile = writeOsis("invalid.xml", "<not-osis/>");
        String missingFile = tempDir.resolve("missing.xml").toString();
        BibleOperations operations = operations(service, new NoopPowerpoint(), config(tempDir.resolve("data")));

        BibleOperations.ImportResult result = operations.importOsis(
            List.of(validFile.toString(), invalidFile.toString(), missingFile));

        assertEquals(List.of(validFile.toString()), result.imported());
        assertEquals(2, result.failures().size());
        assertTrue(result.failures().containsKey(invalidFile.toString()));
        assertTrue(result.failures().containsKey(missingFile));
        assertEquals(List.of("niv"), operations.listVersions());
    }

    @Test
    void validatesBookVersionChapterAndVerseRangesWithoutAssertions() {
        BibleService service = newBibleService(tempDir.resolve("data"));
        BibleOperations operations = operations(service, new NoopPowerpoint(), config(tempDir.resolve("data")));

        assertThrows(IllegalArgumentException.class, () -> operations.getVerses("unknown 1:1", List.of("niv")));
        assertThrows(IllegalArgumentException.class, () -> operations.getVerses("john 1:1", List.of("missing")));
        assertThrows(IllegalArgumentException.class, () -> operations.getVerses("john 22:1", List.of("niv")));
        assertThrows(IllegalArgumentException.class, () -> operations.getVerses("john 1:5-2", List.of("niv")));
        assertThrows(IllegalArgumentException.class, () -> operations.getVerses("john 1:999999999-2000000000", List.of("niv")));
    }

    @Test
    void generatesBiblePowerpointUsingImportedVersesAndCustomTemplate() throws Exception {
        Path dataDir = tempDir.resolve("data");
        Path outputDir = tempDir.resolve("output");
        Path template = tempDir.resolve("templates").resolve("bible.pptx");
        Files.createDirectories(template.getParent());
        Files.copy(Path.of("sample-templates", "template-bible.pptx"), template);
        BibleServiceImpl bibleService = newBibleService(dataDir);
        bibleService.importBible(writeOsis("niv.xml", JOHN_OSIS).toString());

        Map<Class<?>, Object> services = serviceMap();
        Object previousBible = services.put(BibleService.class, bibleService);
        try {
            PowerpointService powerpointService = new PowerpointServiceImpl();
            Config config = config(dataDir);
            config.setOutputDir(outputDir.toString());
            BibleOperations operations = operations(bibleService, powerpointService, config);

            String output = operations.generatePowerpoint("niv - john 1:1-2", template.toString(), "custom/deck.pptx");

            Path generated = Path.of(output);
            assertTrue(Files.isRegularFile(generated), "expected generated deck at " + generated);
            assertTrue(Files.size(generated) > 0);
            assertEquals(outputDir.resolve("custom/deck.pptx").toAbsolutePath().normalize(), generated);
        } finally {
            if (previousBible == null) {
                services.remove(BibleService.class);
            } else {
                services.put(BibleService.class, previousBible);
            }
        }
    }

    private BibleOperations operations(BibleService service, PowerpointService powerpoints, Config config) {
        Supplier<Config> configSupplier = () -> config;
        return new BibleOperations(service, powerpoints, configSupplier, ApplicationExecutionLock.shared());
    }

    private BibleServiceImpl newBibleService(Path dataDir) {
        BibleServiceImpl service = new BibleServiceImpl(dataDir);
        servicesToClose.add(service);
        return service;
    }

    private Config config(Path dataDir) {
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(tempDir.resolve("output").toString());
        config.setBibleVersionToLocale(Map.of("niv", "en_us"));
        config.setDefaultTemplates(Map.of("bible", "bible.pptx"));
        return config;
    }

    private Path writeOsis(String name, String content) throws Exception {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content);
        return path;
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<?>, Object> serviceMap() throws Exception {
        Field servicesField = ServiceProvider.class.getDeclaredField("services");
        servicesField.setAccessible(true);
        return (Map<Class<?>, Object>) servicesField.get(null);
    }

    private static final class NoopPowerpoint implements PowerpointService {
        @Override public void generate(List<Map<String, String>> contents, String template, String output) {}
        @Override public void generate(String reference, String template, String output) {}
        @Override public void generate(Integer id, String template, String output, int lines) {}
        @Override public void generate(com.scottmo.core.songs.api.song.Song song, String template, String output, int lines) {}
        @Override public void generateFromYamlConfigs(String configs, String output) {}
        @Override public void mergeSlideShows(List<String> paths, String output) {}
        @Override public void updateTextFormats(String input, String output, com.scottmo.shared.Range range,
            java.util.regex.Pattern pattern, com.scottmo.shared.TextFormat formats) {}
        @Override public void normalizeNewLines(String input, String output) {}
    }
}
