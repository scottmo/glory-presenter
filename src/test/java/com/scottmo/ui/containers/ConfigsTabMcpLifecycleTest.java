package com.scottmo.ui.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

class ConfigsTabMcpLifecycleTest {
    @TempDir
    Path tempDir;

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void configurationTabToggleStartsAndStopsIsolatedHttpServer() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String javaExecutable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", javaExecutable).toString(),
            "-Djava.awt.headless=true", "-Dglory.mcp.port=" + port,
            "-cp", classPath, ConfigsTabMcpLifecycleHarness.class.getName(), Integer.toString(port)
        ).directory(tempDir.toFile()).start();

        try {
            assertTrue(process.waitFor(24, TimeUnit.SECONDS), "Configuration tab lifecycle harness timed out");
            String stdout = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), "stdout:\n" + stdout + "\nstderr:\n" + stderr);
            assertTrue(stdout.contains("CONFIG_TAB_MCP_LIFECYCLE_OK"), stdout);
            try (var files = Files.walk(tempDir)) {
                assertTrue(files.anyMatch(path -> path.endsWith(Path.of("data", "config.json"))),
                    "child process should initialize its isolated config under the temporary work directory");
            }
        } finally {
            process.destroyForcibly();
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }
}
