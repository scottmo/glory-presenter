package com.scottmo.core.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.ConfigValidator;
import com.scottmo.core.bible.impl.BibleServiceImpl;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongTitle;
import com.scottmo.core.songs.impl.SongServiceImpl;
import com.scottmo.shared.JsonUtil;
import com.scottmo.ui.utils.UiConfigurator;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;

class ConfigOperationsTest {
    private static final String JOHN_OSIS = """
        <osis><osisText osisIDWork="NIV"><div osisID="John"><chapter osisID="John.1">
          <verse osisID="John.1.1">In the beginning was the Word.</verse>
        </chapter></div></osisText></osis>
        """;

    @TempDir
    Path tempDir;

    private SongServiceImpl songs;
    private BibleServiceImpl bible;

    @AfterEach
    void closeStores() throws Exception {
        if (songs != null) {
            songs.close();
        }
        if (bible != null) {
            bible.close();
        }
    }

    @Test
    void saveNormalizesPathsPersistsFullSettingsAndReturnsDefensiveSnapshots() throws Exception {
        ConfigService service = new ConfigService(tempDir.resolve("data/config.json"));
        ConfigOperations operations = operations(service);
        Config config = config(tempDir.resolve("workspace").resolve("data"));
        config.setOutputDir(tempDir.resolve("workspace").resolve("output").toString());
        config.setDarkMode(false);
        config.setUiScale("1.5");
        config.setAppDimensions("1440x900");
        config.setAppFontSize(18);
        config.setLocales(List.of("EN-US", "zh-CN"));
        config.setBibleVersionToLocale(Map.of("NIV", "EN-US"));
        config.setPatternPresets(Map.of("letters", "[A-Z]+"));

        Config saved = operations.saveJson(JsonUtil.saveToString(config));

        assertFalse(saved.isDarkMode());
        assertEquals("1440x900", saved.getAppDimensions());
        assertEquals(18, saved.getAppFontSize());
        assertEquals(List.of("en_us", "zh_cn"), saved.getLocales());
        assertEquals(Map.of("niv", "en_us"), saved.getBibleVersionToLocale());
        assertEquals(Path.of(config.getDataDir()).normalize().toString(), saved.getDataDir());
        assertTrue(Files.isDirectory(Path.of(saved.getDataDir(), Config.TEMPLATE_DIR)));
        assertTrue(Files.isDirectory(Path.of(saved.getOutputDir())));
        assertTrue(Files.readString(service.getConfigPath()).contains("\"darkMode\" : false"));

        Config snapshot = operations.snapshot();
        snapshot.setDarkMode(true);
        snapshot.getLocales().clear();
        assertFalse(operations.snapshot().isDarkMode());
        assertEquals(List.of("en_us", "zh_cn"), operations.snapshot().getLocales());
    }

    @Test
    void rejectsMalformedConfigurationWithoutReplacingFileOrActiveSnapshot() throws Exception {
        ConfigService service = new ConfigService(tempDir.resolve("data/config.json"));
        ConfigOperations operations = operations(service);
        Config saved = operations.saveJson(JsonUtil.saveToString(config(tempDir.resolve("data-root"))));
        String originalFile = Files.readString(service.getConfigPath());
        String originalDataDir = saved.getDataDir();

        assertThrows(IllegalArgumentException.class, () -> operations.saveJson("{\"dataDir\":null}"));
        assertThrows(IllegalArgumentException.class, () -> operations.saveJson("{\"uiScale\":\"nan\"}"));
        Config invalidPattern = config(tempDir.resolve("unused"));
        invalidPattern.setPatternPresets(Map.of("bad", "["));
        assertThrows(IllegalArgumentException.class,
            () -> operations.saveJson(JsonUtil.saveToString(invalidPattern)));

        assertEquals(originalFile, Files.readString(service.getConfigPath()));
        assertEquals(originalDataDir, operations.snapshot().getDataDir());
    }

    @Test
    void reloadActivatesValidatedDiskChangesAndThemePreferenceIsPersisted() throws Exception {
        ConfigService service = new ConfigService(tempDir.resolve("data/config.json"));
        ConfigOperations operations = operations(service);
        operations.saveJson(JsonUtil.saveToString(config(tempDir.resolve("data-root"))));

        Config externallyUpdated = config(tempDir.resolve("reloaded-data"));
        externallyUpdated.setDarkMode(false);
        externallyUpdated.setLocales(List.of("fr-FR"));
        Files.writeString(service.getConfigPath(), JsonUtil.saveToString(externallyUpdated));
        Config reloaded = operations.reload();
        assertFalse(reloaded.isDarkMode());
        assertEquals(List.of("fr_fr"), reloaded.getLocales());
        assertTrue(Path.of(reloaded.getDataDir()).endsWith("reloaded-data"));

        Config themed = operations.setDarkMode(true);
        assertTrue(themed.isDarkMode());
        assertTrue(Files.readString(service.getConfigPath()).contains("\"darkMode\" : true"));
        assertNotEquals("fr-FR", operations.snapshot().getLocales().get(0));
    }

    @Test
    void changingDataDirSwitchesCachedSongAndBibleStoresToTheNewLocation() throws Exception {
        Path configFile = tempDir.resolve("config/config.json");
        ConfigService service = new ConfigService(configFile);
        ConfigOperations operations = operations(service);
        Path originalData = tempDir.resolve("original-data");
        operations.saveJson(JsonUtil.saveToString(config(originalData)));

        songs = new SongServiceImpl(service);
        Song song = new Song(-1).setTitles(List.of(new SongTitle("A Song", "en_us")));
        songs.store(song);
        bible = new BibleServiceImpl(service);
        Path osisFile = tempDir.resolve("john.xml");
        Files.writeString(osisFile, JOHN_OSIS);
        bible.importBible(osisFile.toString());
        assertEquals(List.of("niv"), bible.getAvailableVersions());

        Path newData = tempDir.resolve("new-data");
        operations.saveJson(JsonUtil.saveToString(config(newData)));

        assertTrue(songs.getAllSongDescriptors(List.of("en_us")).isEmpty());
        assertTrue(bible.getAvailableVersions().isEmpty());
        assertTrue(Files.isRegularFile(originalData.resolve("songs.db")));
        assertTrue(Files.isRegularFile(originalData.resolve("bible.db")));
        assertTrue(Files.isRegularFile(newData.resolve("songs.db")));
        assertTrue(Files.isRegularFile(newData.resolve("bible.db")));
    }

    @Test
    void normalizesRelativePathsAndValidatesConfigurationMaps() {
        Config relative = config(Path.of("relative-data"));
        relative.setOutputDir("relative-output");
        relative.setUiFontFamily("");
        relative.setPatternPresets(Map.of("match-all", ""));
        Config normalized = ConfigValidator.normalizeAndValidate(relative);
        assertEquals(Path.of("relative-data").normalize().toString(), normalized.getDataDir());
        assertEquals(Path.of("relative-output").normalize().toString(), normalized.getOutputDir());
        relative.setUiScale("144dpi");
        assertEquals("144dpi", ConfigValidator.normalizeAndValidate(relative).getUiScale());
        relative.setDataDir(".");
        relative.setOutputDir(".");
        Config currentDirectory = ConfigValidator.normalizeAndValidate(relative);
        assertEquals(".", currentDirectory.getDataDir());
        assertEquals(".", currentDirectory.getOutputDir());
        assertThrows(IllegalArgumentException.class, () -> {
            Config invalid = config(tempDir.resolve("invalid"));
            invalid.setLocales(List.of("en-us", "EN_US"));
            ConfigValidator.normalizeAndValidate(invalid);
        });
    }

    @Test
    void appliesRequestedThemeOnEdtWithoutRequiringAnAppFrame() throws Exception {
        ConfigService service = new ConfigService(tempDir.resolve("config/config.json"));
        ConfigOperations operations = new ConfigOperations(service, ApplicationExecutionLock.shared(),
            UiConfigurator::applyConfiguration);
        Config config = config(tempDir.resolve("theme-data"));
        config.setUiFontFamily("Dialog");
        config.setAppFontSize(18);
        operations.saveJson(JsonUtil.saveToString(config));
        operations.setDarkMode(false);
        SwingUtilities.invokeAndWait(() -> {});
        assertTrue(UIManager.getLookAndFeel() instanceof FlatCyanLightIJTheme);
        assertEquals(18, UIManager.getFont("Label.font").getSize());

        operations.setDarkMode(true);
        SwingUtilities.invokeAndWait(() -> {});
        assertTrue(UIManager.getLookAndFeel() instanceof FlatCarbonIJTheme);
        assertEquals(18, UIManager.getFont("Label.font").getSize());
    }

    private ConfigOperations operations(ConfigService service) {
        return new ConfigOperations(service, ApplicationExecutionLock.shared());
    }

    private Config config(Path dataDir) {
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(tempDir.resolve("output").toString());
        config.setLocales(List.of("en_us"));
        config.setBibleVersionToLocale(Map.of("niv", "en_us"));
        config.setDefaultTemplates(Map.of("song", "song.pptx", "bible", "bible.pptx"));
        config.setPatternPresets(Map.of("letters", "[A-Za-z]+"));
        return config;
    }
}
