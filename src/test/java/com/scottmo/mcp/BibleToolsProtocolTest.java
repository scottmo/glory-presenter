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

class BibleToolsProtocolTest {
    private static final ObjectMapper JSON = new ObjectMapper();
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

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void importsAndQueriesBibleAndReturnsInvalidReferenceAsToolError() throws Exception {
        Path workDir = tempDir.resolve("process");
        Path dataDir = tempDir.resolve("app-data");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(workDir.resolve("data"));
        Files.createDirectories(dataDir.resolve("templates"));
        Path osisFile = workDir.resolve("niv.xml");
        Files.writeString(osisFile, JOHN_OSIS);

        Config config = new Config();
        config.setDataDir(dataDir.toString());
        config.setOutputDir(outputDir.toString());
        config.setLocales(List.of("en_us"));
        config.setBibleVersionToLocale(Map.of("niv", "en_us"));
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
                    "clientInfo", Map.of("name", "bible-test", "version", "1"))));
            assertFalse(initialized.has("error"), initialized.toString());
            notifyInitialized(input);

            JsonNode tools = request(input, output, Map.of("jsonrpc", "2.0", "id", 2,
                "method", "tools/list", "params", Map.of()));
            assertTrue(tools.path("result").path("tools").toString().contains("bible_import_osis"));
            assertTrue(tools.path("result").path("tools").toString().contains("bible_get_verses"));

            JsonNode imported = callTool(input, output, 3, "bible_import_osis", Map.of("filePaths", List.of(osisFile.toString())));
            assertFalse(imported.path("result").path("isError").asBoolean(), imported.toString());
            JsonNode importPayload = JSON.readTree(content(imported));
            assertEquals(osisFile.toString(), importPayload.path("imported").get(0).asText());
            assertEquals(0, importPayload.path("failures").size());

            JsonNode versions = callTool(input, output, 4, "bible_list_versions", Map.of());
            JsonNode versionPayload = JSON.readTree(content(versions));
            assertEquals("niv", versionPayload.get(0).asText());

            JsonNode verses = callTool(input, output, 5, "bible_get_verses", Map.of("reference", "john 1:1-2"));
            assertFalse(verses.path("result").path("isError").asBoolean(), verses.toString());
            JsonNode versePayload = JSON.readTree(content(verses));
            assertEquals("In the beginning was the Word.", versePayload.path("niv").get(0).path("text").asText());
            assertEquals("He was with God in the beginning.", versePayload.path("niv").get(1).path("text").asText());

            JsonNode invalid = callTool(input, output, 6, "bible_get_verses", Map.of("reference", "john 22:1"));
            assertTrue(invalid.path("result").path("isError").asBoolean(), invalid.toString());
            assertTrue(content(invalid).contains("Chapter 22"), invalid.toString());

            JsonNode partialImport = callTool(input, output, 7, "bible_import_osis",
                Map.of("filePaths", List.of(osisFile.toString(), workDir.resolve("missing.xml").toString())));
            assertTrue(partialImport.path("result").path("isError").asBoolean(), partialImport.toString());
            JsonNode partialPayload = JSON.readTree(content(partialImport));
            assertEquals(1, partialPayload.path("imported").size());
            assertEquals(1, partialPayload.path("failures").size());

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

    private static String content(JsonNode response) {
        return response.path("result").path("content").get(0).path("text").asText();
    }

    private static void notifyInitialized(BufferedWriter input) throws Exception {
        input.write("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        input.newLine();
        input.flush();
    }
}
