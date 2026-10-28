package com.scottmo.core.operations;

import com.scottmo.core.ppt.impl.PowerpointServiceImpl;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ProgramOperationsTest {
    @TempDir Path tempDir;

    @Test
    void recursivelyListsAndReadsTemplatesUsingPortablePaths() throws Exception {
        Path templates = Files.createDirectories(tempDir.resolve("templates"));
        Path nested = Files.createDirectories(templates.resolve("nested"));
        Files.writeString(nested.resolve("program-service.txt"), "service yaml");
        Files.writeString(nested.resolve("song.pptx"), "template bytes");
        ProgramOperations operations = operations(templates, tempDir.resolve("output"));

        assertEquals(List.of("nested/program-service.txt", "nested/song.pptx"), operations.listTemplates());
        assertEquals(List.of("nested/program-service.txt"), operations.listProgramTemplates());
        assertEquals("service yaml", operations.readProgramTemplate("nested/program-service.txt"));
        assertThrows(IllegalArgumentException.class, () -> operations.readTemplate("../outside.txt"));
        assertThrows(IllegalArgumentException.class, () -> operations.readProgramTemplate("nested/song.pptx"));
    }

    @Test
    void generatesAndMergesListOfMapsUsingFirstMapAsMetadata() throws Exception {
        Path templates = Files.createDirectories(tempDir.resolve("ppt-templates"));
        createTemplate(templates.resolve("default.pptx"));
        PowerpointServiceImpl service = new PowerpointServiceImpl(templates, Map.of("default", "default.pptx"));
        ProgramOperations operations = operations(templates, tempDir.resolve("output"), service);
        String yaml = """
            - type: default
              content: |
                - {title: Opening}
                - {title: First}
                - {title: Second}
            - type: default
              content: |
                - {title: Second set}
                - {title: Third}
            """;

        Path output = operations.generate(yaml);
        assertEquals(tempDir.resolve("output/output.pptx").toAbsolutePath().normalize(), output);
        try (XMLSlideShow ppt = new XMLSlideShow(Files.newInputStream(output))) {
            assertEquals(7, ppt.getSlides().size());
            List<String> slideTexts = ppt.getSlides().stream().map(ProgramOperationsTest::slideText).toList();
            assertEquals(List.of("Opening", "First", "Second", "Opening", "Second set", "Third", "Second set"), slideTexts);
        }

        Path explicitOutput = tempDir.resolve("custom/location/program.pptx");
        assertEquals(explicitOutput, operations.generate(yaml, explicitOutput.toString()));
        assertTrue(Files.isRegularFile(explicitOutput));
        Path relativeOutput = tempDir.resolve("output/relative/program.pptx").toAbsolutePath().normalize();
        assertEquals(relativeOutput, operations.generate(yaml, "relative/program.pptx"));
    }

    @Test
    void preservesStaticFirstAndLastSlidesWhenMetadataMapIsEmpty() throws Exception {
        Path templates = Files.createDirectories(tempDir.resolve("static-templates"));
        createTemplate(templates.resolve("default.pptx"), true);
        PowerpointServiceImpl service = new PowerpointServiceImpl(templates, Map.of("default", "default.pptx"));
        ProgramOperations operations = operations(templates, tempDir.resolve("static-output"), service);
        Path output = operations.generate("""
            - type: default
              content: |
                - {}
                - {title: Middle}
            """);

        try (XMLSlideShow ppt = new XMLSlideShow(Files.newInputStream(output))) {
            assertEquals(List.of("Opening", "Middle", "Closing"),
                ppt.getSlides().stream().map(ProgramOperationsTest::slideText).toList());
        }
    }

    @Test
    void reportsInvalidYamlAndRemovesOnlyItsTemporaryFiles() throws Exception {
        Path templates = Files.createDirectories(tempDir.resolve("yaml-templates"));
        createTemplate(templates.resolve("default.pptx"));
        PowerpointServiceImpl service = new PowerpointServiceImpl(templates, Map.of("default", "default.pptx"));
        ProgramOperations operations = operations(templates, tempDir.resolve("yaml-output"), service);
        Path unrelated = Files.createTempFile("glory-presenter-unrelated-", ".pptx");
        List<Path> before;
        try (Stream<Path> paths = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            before = paths.filter(path -> path.getFileName().toString().startsWith("glory-presenter-")).toList();
        }

        IOException error = assertThrows(IOException.class, () -> operations.generate("- type: [broken"));
        assertTrue(error.getMessage().toLowerCase().contains("yaml"));
        assertTrue(Files.exists(unrelated));

        assertThrows(IOException.class, () -> operations.generate("""
            - type: default
              template: default.pptx
              content: |
                - {title: First}
            - type: default
              template: missing.pptx
              content: |
                - {title: Second}
            """));
        try (Stream<Path> paths = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            List<Path> after = paths.filter(path -> path.getFileName().toString().startsWith("glory-presenter-"))
                .sorted().toList();
            assertEquals(before.stream().sorted().toList(), after);
        } finally {
            Files.deleteIfExists(unrelated);
        }
    }

    @Test
    void rejectsTemplatesOutsideInjectedRootDuringGeneration() throws Exception {
        Path templates = Files.createDirectories(tempDir.resolve("secure-templates"));
        createTemplate(templates.resolve("default.pptx"));
        PowerpointServiceImpl service = new PowerpointServiceImpl(templates, Map.of("default", "default.pptx"));
        ProgramOperations operations = operations(templates, tempDir.resolve("secure-output"), service);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> operations.generate("""
            - type: default
              template: ../outside.pptx
              content: |
                - {title: Unsafe}
            """));
        assertTrue(error.getMessage().contains("escapes"));
    }

    private ProgramOperations operations(Path templates, Path output) {
        return operations(templates, output, new PowerpointServiceImpl(templates, Map.of("default", "default.pptx")));
    }

    private ProgramOperations operations(Path templates, Path output, PowerpointServiceImpl service) {
        return new ProgramOperations(templates, output, service);
    }

    private static void createTemplate(Path path) throws IOException {
        createTemplate(path, false);
    }

    private static void createTemplate(Path path, boolean staticEnds) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            int i = 0;
            for (String label : List.of("start", "content", "end")) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextBox box = slide.createTextBox();
                box.setText(staticEnds && i != 1 ? (i == 0 ? "Opening" : "Closing") : "{title}");
                box.setAnchor(new java.awt.Rectangle(20, 20, 300, 50));
                i++;
            }
            try (var out = Files.newOutputStream(path)) {
                ppt.write(out);
            }
        }
    }

    private static String slideText(XSLFSlide slide) {
        return slide.getShapes().stream()
            .filter(org.apache.poi.xslf.usermodel.XSLFTextShape.class::isInstance)
            .map(org.apache.poi.xslf.usermodel.XSLFTextShape.class::cast)
            .map(org.apache.poi.xslf.usermodel.XSLFTextShape::getText)
            .findFirst().orElse("");
    }
}
