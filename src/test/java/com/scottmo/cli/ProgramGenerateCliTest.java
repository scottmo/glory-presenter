package com.scottmo.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramGenerateCliTest {
    @TempDir Path tempDir;

    @Test
    void helpDoesNotRequireConfigurationOrStartTheGui() throws Exception {
        ProcessResult rootHelp = run("--help");
        assertEquals(0, rootHelp.exitCode());
        assertTrue(rootHelp.stdout().contains("program generate"));
        assertEquals("", rootHelp.stderr());
        assertFalse(Files.exists(tempDir.resolve("data")), "help must not initialize app data");

        ProcessResult result = run("program", "generate", "--help");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("--config <file>"));
        assertTrue(result.stdout().contains("not data/config.json"));
        assertEquals("", result.stderr());
        assertFalse(Files.exists(tempDir.resolve("data")), "help must not initialize app data");
    }

    @Test
    void missingConfigAndUnknownOptionsReturnUsageErrors() throws Exception {
        ProcessResult missing = run("program", "generate");
        assertEquals(2, missing.exitCode());
        assertTrue(missing.stderr().contains("--config is required"));
        assertEquals("", missing.stdout());

        ProcessResult unknown = run("program", "generate", "--config", "program.yaml", "--wat");
        assertEquals(2, unknown.exitCode());
        assertTrue(unknown.stderr().contains("unknown option: --wat"));
        assertEquals("", unknown.stdout());

        ProcessResult missingValue = run("program", "generate", "--config", "-h");
        assertEquals(2, missingValue.exitCode());
        assertTrue(missingValue.stderr().contains("--config requires"));

        ProcessResult mcpAsValue = run("program", "generate", "--config", "--mcp-server");
        assertEquals(2, mcpAsValue.exitCode());
        assertTrue(mcpAsValue.stderr().contains("--config requires"));
        assertFalse(Files.exists(tempDir.resolve("data")), "invalid command must not start the MCP server");
    }

    @Test
    void generatesDeckHeadlesslyAndKeepsStdoutAsJsonResult() throws Exception {
        Files.createDirectories(tempDir.resolve("data/templates"));
        Files.createDirectories(tempDir.resolve("输出"));
        createTemplate(tempDir.resolve("data/templates/default.pptx"));
        Files.writeString(tempDir.resolve("data/config.json"), """
            {
              "dataDir": "data",
              "outputDir": "输出",
              "defaultTemplates": {"default": "default.pptx"}
            }
            """, StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("service.yaml"), """
            - type: default
              content: |
                - {}
                - {title: "晨曦敬拜"}
            """, StandardCharsets.UTF_8);

        ProcessResult result = run("program", "generate", "--config", "service.yaml", "--json");

        assertEquals(0, result.exitCode(), result.stderr());
        JsonNode json = new ObjectMapper().readTree(result.stdout());
        Path output = Path.of(json.get("outputPath").asText());
        assertEquals(tempDir.resolve("输出/output.pptx").toAbsolutePath().normalize(), output);
        try (XMLSlideShow deck = new XMLSlideShow(Files.newInputStream(output))) {
            List<String> text = deck.getSlides().stream()
                .flatMap(slide -> slide.getShapes().stream())
                .filter(org.apache.poi.xslf.usermodel.XSLFTextShape.class::isInstance)
                .map(org.apache.poi.xslf.usermodel.XSLFTextShape.class::cast)
                .map(org.apache.poi.xslf.usermodel.XSLFTextShape::getText)
                .toList();
            assertTrue(text.contains("晨曦敬拜"), text.toString());
        }

        ProcessResult humanResult = run("program", "generate", "--config", "service.yaml",
            "--output", "custom/program.pptx");
        assertEquals(0, humanResult.exitCode(), humanResult.stderr());
        assertEquals(tempDir.resolve("输出/custom/program.pptx").toAbsolutePath().normalize() + System.lineSeparator(),
            humanResult.stdout());
        assertTrue(Files.isRegularFile(tempDir.resolve("输出/custom/program.pptx")));
        try (XMLSlideShow deck = new XMLSlideShow(Files.newInputStream(tempDir.resolve("输出/custom/program.pptx")))) {
            assertTrue(deck.getSlides().stream().flatMap(slide -> slide.getShapes().stream())
                .filter(org.apache.poi.xslf.usermodel.XSLFTextShape.class::isInstance)
                .map(org.apache.poi.xslf.usermodel.XSLFTextShape.class::cast)
                .map(org.apache.poi.xslf.usermodel.XSLFTextShape::getText)
                .anyMatch("晨曦敬拜"::equals));
        }
    }

    @Test
    void unreadableInputAndInvalidYamlFailWithoutPrintingAResult() throws Exception {
        ProcessResult unreadable = run("program", "generate", "--config", "missing.yaml", "--json");
        assertEquals(1, unreadable.exitCode());
        assertTrue(unreadable.stderr().contains("missing.yaml"));
        assertEquals("", unreadable.stdout());
        assertFalse(Files.exists(tempDir.resolve("data")), "unreadable input must fail before app initialization");

        createFixture();
        Files.writeString(tempDir.resolve("broken.yaml"), "- type: [broken", StandardCharsets.UTF_8);
        ProcessResult invalid = run("program", "generate", "--config", "broken.yaml", "--json");
        assertEquals(1, invalid.exitCode());
        assertTrue(invalid.stderr().toLowerCase().contains("yaml"), invalid.stderr());
        assertEquals("", invalid.stdout());
        assertFalse(Files.exists(tempDir.resolve("输出/output.pptx")));
    }

    private ProcessResult run(String... args) throws Exception {
        var command = new java.util.ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Djava.awt.headless=true");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("com.scottmo.App");
        command.addAll(List.of(args));
        Path stdout = Files.createTempFile(tempDir, "cli-stdout-", ".txt");
        Path stderr = Files.createTempFile(tempDir, "cli-stderr-", ".txt");
        Process process = new ProcessBuilder(command).directory(tempDir.toFile())
            .redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
        try {
            if (!process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                fail("CLI process timed out: " + command);
            }
            return new ProcessResult(process.exitValue(), Files.readString(stdout, StandardCharsets.UTF_8),
                Files.readString(stderr, StandardCharsets.UTF_8));
        } finally {
            if (process.isAlive()) process.destroyForcibly();
            Files.deleteIfExists(stdout);
            Files.deleteIfExists(stderr);
        }
    }

    private void createFixture() throws Exception {
        Files.createDirectories(tempDir.resolve("data/templates"));
        Files.createDirectories(tempDir.resolve("输出"));
        createTemplate(tempDir.resolve("data/templates/default.pptx"));
        Files.writeString(tempDir.resolve("data/config.json"), """
            {
              "dataDir": "data",
              "outputDir": "输出",
              "defaultTemplates": {"default": "default.pptx"}
            }
            """, StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("service.yaml"), """
            - type: default
              content: |
                - {}
                - {title: "晨曦敬拜"}
            """, StandardCharsets.UTF_8);
    }

    private static void createTemplate(Path path) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            for (String label : List.of("static start", "{title}", "static end")) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox box = slide.createTextBox();
                box.setText(label);
                box.setAnchor(new Rectangle(20, 20, 300, 50));
            }
            try (var out = Files.newOutputStream(path)) {
                ppt.write(out);
            }
        }
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {}
}
