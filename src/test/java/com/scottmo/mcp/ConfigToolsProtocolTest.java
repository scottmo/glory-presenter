package com.scottmo.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.config.Config;

class ConfigToolsProtocolTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void configToolsReadSaveReloadAndPersistThemeThroughStdio() throws Exception {
        Path workDir = tempDir.resolve("process");
        Path dataDir = tempDir.resolve("app-data");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(workDir.resolve("data"));
        Config config = config(dataDir, outputDir);
        Path configFile = workDir.resolve(Config.CONFIG_FILENAME);
        Files.writeString(configFile, JSON.writeValueAsString(config));

        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String javaExecutable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", javaExecutable).toString(),
            "-Djava.awt.headless=true", "-cp", classPath, "com.scottmo.App", "--mcp-server"
        ).directory(workDir.toFile()).start();

        try (BufferedWriter input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            JsonNode initialize = request(input, output, Map.of("jsonrpc", "2.0", "id", 1, "method", "initialize",
                "params", Map.of("protocolVersion", "2025-11-25", "capabilities", Map.of(),
                    "clientInfo", Map.of("name", "config-test", "version", "1"))));
            assertFalse(initialize.has("error"), initialize.toString());
            notifyInitialized(input);

            JsonNode tools = request(input, output, Map.of("jsonrpc", "2.0", "id", 2, "method", "tools/list", "params", Map.of()));
            assertTrue(tools.path("result").path("tools").toString().contains("config_save"));

            JsonNode initial = call(input, output, 3, "config_get", Map.of());
            JsonNode initialConfig = resultJson(initial);
            assertTrue(initialConfig.path("darkMode").asBoolean());

            JsonNode changedTheme = call(input, output, 4, "config_set_dark_mode", Map.of("darkMode", false));
            assertFalse(changedTheme.path("result").path("isError").asBoolean(), changedTheme.toString());
            assertFalse(resultJson(changedTheme).path("darkMode").asBoolean());
            assertFalse(JSON.readTree(Files.readString(configFile)).path("darkMode").asBoolean());

            Config externallyChanged = JSON.treeToValue(JSON.readTree(Files.readString(configFile)), Config.class);
            externallyChanged.setDarkMode(true);
            externallyChanged.setUiScale("144dpi");
            Files.writeString(configFile, JSON.writeValueAsString(externallyChanged));
            JsonNode reloaded = call(input, output, 5, "config_reload", Map.of());
            assertTrue(resultJson(reloaded).path("darkMode").asBoolean());
            assertEquals("144dpi", resultJson(reloaded).path("uiScale").asText());

            JsonNode current = call(input, output, 6, "config_get", Map.of());
            Config update = JSON.treeToValue(resultJson(current), Config.class);
            update.setAppFontSize(22);
            JsonNode saved = call(input, output, 7, "config_save", Map.of("json", JSON.writeValueAsString(update)));
            assertFalse(saved.path("result").path("isError").asBoolean(), saved.toString());
            assertEquals(22, resultJson(saved).path("appFontSize").asInt());

            String beforeInvalidWrite = Files.readString(configFile);
            JsonNode invalid = call(input, output, 8, "config_save", Map.of("json", "{\"dataDir\":null}"));
            assertTrue(invalid.path("result").path("isError").asBoolean(), invalid.toString());
            assertEquals(beforeInvalidWrite, Files.readString(configFile));

            input.close();
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "MCP process did not stop on stdin EOF");
            assertEquals(0, process.exitValue(), new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            process.destroyForcibly();
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }

    private Config config(Path dataDir, Path outputDir) {
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(outputDir.toString());
        config.setLocales(List.of("en_us"));
        config.setDefaultTemplates(Map.of("song", "song.pptx"));
        config.setBibleVersionToLocale(Map.of("niv", "en_us"));
        config.setTextFormatPresets(Map.of());
        config.setPatternPresets(Map.of("title", "^.+$"));
        return config;
    }

    private static JsonNode call(BufferedWriter input, BufferedReader output, int id, String name,
        Map<String, Object> arguments) throws Exception {
        return request(input, output, Map.of("jsonrpc", "2.0", "id", id, "method", "tools/call",
            "params", Map.of("name", name, "arguments", arguments)));
    }

    private static JsonNode resultJson(JsonNode response) throws Exception {
        return JSON.readTree(response.path("result").path("content").get(0).path("text").asText());
    }

    private static JsonNode request(BufferedWriter input, BufferedReader output, Map<String, Object> message)
        throws Exception {
        input.write(JSON.writeValueAsString(message));
        input.newLine();
        input.flush();
        String response = output.readLine();
        if (response == null) {
            throw new AssertionError("MCP server closed stdout before responding to " + message.get("method"));
        }
        return JSON.readTree(response);
    }

    private static void notifyInitialized(BufferedWriter input) throws Exception {
        input.write("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        input.newLine();
        input.flush();
    }
}
