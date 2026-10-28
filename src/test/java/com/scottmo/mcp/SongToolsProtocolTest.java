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

class SongToolsProtocolTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void songToolsCanCreateSearchGetUpdateExportAndDeleteThroughStdio() throws Exception {
        Path workDir = tempDir.resolve("process");
        Path dataDir = tempDir.resolve("app-data");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(workDir.resolve("data"));
        Files.createDirectories(dataDir.resolve("templates"));
        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(outputDir.toString());
        config.setLocales(List.of("en_us"));
        Files.writeString(workDir.resolve(Config.CONFIG_FILENAME), JSON.writeValueAsString(config));

        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String javaExecutable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", javaExecutable).toString(),
            "-Djava.awt.headless=true", "-cp", classPath, "com.scottmo.App", "--mcp-server"
        ).directory(workDir.toFile()).start();

        try (BufferedWriter input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            JsonNode initialized = request(input, output, Map.of("jsonrpc", "2.0", "id", 1, "method", "initialize",
                "params", Map.of("protocolVersion", "2025-11-25", "capabilities", Map.of(),
                    "clientInfo", Map.of("name", "song-test", "version", "1"))));
            assertFalse(initialized.has("error"), initialized.toString());
            notifyInitialized(input);

            JsonNode listTools = request(input, output, Map.of("jsonrpc", "2.0", "id", 2,
                "method", "tools/list", "params", Map.of()));
            assertTrue(listTools.path("result").path("tools").toString().contains("song_create"));

            Map<String, Object> song = Map.of(
                "titles", List.of(Map.of("text", "Grace Song", "locale", "en_us")),
                "authors", List.of("Test Author"),
                "verses", List.of(Map.of("name", "v1", "text", "Wonderful grace", "locale", "en_us")),
                "verseOrder", List.of("v1"));
            JsonNode created = callTool(input, output, 3, "song_create", Map.of("song", song));
            assertFalse(created.path("result").path("isError").asBoolean(), created.toString());
            JsonNode createdPayload = JSON.readTree(created.path("result").path("content").get(0).path("text").asText());
            int id = createdPayload.path("id").asInt();
            assertTrue(id > 0);

            JsonNode searched = callTool(input, output, 4, "songs_list", Map.of("query", "grace"));
            JsonNode searchPayload = JSON.readTree(searched.path("result").path("content").get(0).path("text").asText());
            assertEquals(id, searchPayload.get(0).path("id").asInt());

            JsonNode fetched = callTool(input, output, 5, "song_get", Map.of("id", id));
            JsonNode fetchedSong = JSON.readTree(fetched.path("result").path("content").get(0).path("text").asText());
            assertEquals("Grace Song", fetchedSong.path("titles").get(0).path("text").asText());

            Map<String, Object> updatedSong = Map.of(
                "titles", List.of(Map.of("text", "Updated Grace", "locale", "en_us")),
                "authors", List.of("Test Author"),
                "verses", List.of(Map.of("name", "v1", "text", "Amazing grace", "locale", "en_us")),
                "verseOrder", List.of("v1"));
            JsonNode updated = callTool(input, output, 6, "song_update", Map.of("id", id, "song", updatedSong));
            JsonNode updatedPayload = JSON.readTree(updated.path("result").path("content").get(0).path("text").asText());
            assertEquals("Updated Grace", updatedPayload.path("song").path("titles").get(0).path("text").asText());

            Path xml = outputDir.resolve("exported/song.xml");
            JsonNode exported = callTool(input, output, 7, "song_export_openlyrics", Map.of("id", id, "path", xml.toString()));
            assertFalse(exported.path("result").path("isError").asBoolean(), exported.toString());
            JsonNode exportPayload = JSON.readTree(exported.path("result").path("content").get(0).path("text").asText());
            assertTrue(Files.isRegularFile(xml));
            assertTrue(exportPayload.path("content").asText().contains("Updated Grace"));

            JsonNode invalid = callTool(input, output, 8, "song_get", Map.of("id", 99999));
            assertTrue(invalid.path("result").path("isError").asBoolean(), invalid.toString());

            JsonNode deleted = callTool(input, output, 9, "songs_delete", Map.of("ids", List.of(id)));
            assertFalse(deleted.path("result").path("isError").asBoolean(), deleted.toString());
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

    private static JsonNode callTool(BufferedWriter input, BufferedReader output, int id, String name,
        Map<String, Object> arguments) throws Exception {
        return request(input, output, Map.of("jsonrpc", "2.0", "id", id, "method", "tools/call",
            "params", Map.of("name", name, "arguments", arguments)));
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
