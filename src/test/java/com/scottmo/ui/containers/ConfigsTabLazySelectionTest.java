package com.scottmo.ui.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.config.Config;

class ConfigsTabLazySelectionTest {
    @TempDir
    Path tempDir;

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void lazyConfigTabSelectionDefersFlatLafUpdateUntilChangeDispatchFinishes() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "This regression exercises the native Swing mouse event path");
        Path childDir = tempDir.resolve("child");
        Path dataDir = tempDir.resolve("app-data");
        Files.createDirectories(childDir.resolve("data"));
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(tempDir.resolve("output").toString());
        config.setLocales(List.of("en_us"));
        config.setDarkMode(false);
        config.setBibleVersionToLocale(Map.of());
        config.setDefaultTemplates(Map.of());
        config.setPatternPresets(Map.of());
        config.setTextFormatPresets(Map.of());
        Files.writeString(childDir.resolve(Config.CONFIG_FILENAME), new ObjectMapper().writeValueAsString(config));

        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String javaExecutable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", javaExecutable).toString(),
            "-Djava.awt.headless=false", "-cp", classPath, ConfigsTabLazySelectionHarness.class.getName()
        ).directory(childDir.toFile()).start();

        try {
            assertTrue(process.waitFor(24, TimeUnit.SECONDS), "lazy-tab regression harness timed out");
            String stdout = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), "stdout:\n" + stdout + "\nstderr:\n" + stderr);
            assertTrue(stdout.contains("CONFIG_TAB_LAZY_SELECTION_OK"), stdout);
            try (var files = Files.walk(childDir)) {
                assertTrue(files.anyMatch(path -> path.endsWith(Path.of("data", "config.json"))),
                    "all config reads and writes must stay in the isolated child working directory");
            }
        } finally {
            process.destroyForcibly();
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }
}
