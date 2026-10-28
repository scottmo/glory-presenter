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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class McpStdioServerTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void runsInitializeToolsAndToolCallThenExitsOnEof() throws Exception {
        Path workDir = Files.createTempDirectory("glory-mcp-stdio-");
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String javaExecutable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", javaExecutable).toString(),
            "-Djava.awt.headless=true", "-cp", classPath, "com.scottmo.App", "--mcp-server"
        ).directory(workDir.toFile()).start();

        try {
        BufferedWriter input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        input.write("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test-client\",\"version\":\"1\"}}}");
        input.newLine();
        input.flush();
        JsonNode initialize = JSON.readTree(output.readLine());

        input.write("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        input.newLine();
        input.write("{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");
        input.newLine();
        input.flush();
        JsonNode list = JSON.readTree(output.readLine());

        input.write("{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"mcp_status\",\"arguments\":{}}}");
        input.newLine();
        input.flush();
        JsonNode call = JSON.readTree(output.readLine());
        input.close();

        boolean exited = process.waitFor(15, TimeUnit.SECONDS);
        if (!exited) {
            process.destroyForcibly();
            throw new AssertionError("MCP process did not exit after stdin EOF");
        }
        String trailingOutput = output.lines().reduce("", (a, b) -> a + b + "\n");
        String diagnostics = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), diagnostics);

        assertEquals("2025-11-25", initialize.path("result").path("protocolVersion").asText());
        assertFalse(initialize.has("error"), initialize.toString());
        assertTrue(list.path("result").path("tools").toString().contains("mcp_status"), list.toString());
        assertTrue(call.path("result").path("content").toString().contains("Glory Presenter"), call.toString());
        assertFalse(trailingOutput.contains("Loading default config.json"), "application logs must not appear on protocol stdout");
        try (var files = Files.list(workDir)) {
            assertTrue(files.findAny().isEmpty(), "status-only server must not touch app data");
        }
        } finally {
            process.destroyForcibly();
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
            try (var files = Files.list(workDir)) {
                for (Path path : files.toList()) {
                    Files.deleteIfExists(path);
                }
            }
            Files.deleteIfExists(workDir);
        }
    }
}
