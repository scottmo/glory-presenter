package com.scottmo.core.operations;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Rectangle;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.scottmo.config.Config;
import com.scottmo.core.operations.ApplicationExecutionLock;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.core.ppt.impl.PowerpointServiceImpl;
import com.scottmo.shared.TextFormat;

class FormatterOperationsTest {
    @TempDir
    Path tempDir;

    @Test
    void formatsMatchingRunsOnlyInSelectedSlideRangeWithoutOverwritingSource() throws Exception {
        Path input = tempDir.resolve("slides.pptx");
        createDeck(input, List.of(
            List.of("target on first slide"),
            List.of("target on selected slide", "unrelated text"),
            List.of("target on last slide")
        ));
        FormatterOperations operations = operations(config());

        FormatterOperations.BatchResult result = operations.formatText(
            List.of(input.toString()), 1, 2, "target", "fontSize: 24\nbold: true", false);

        assertEquals(1, result.successes().size());
        assertTrue(result.failures().isEmpty());
        Path output = Path.of(result.successes().get(0).outputPath());
        assertEquals(tempDir.resolve("slides.mod.pptx"), output);
        assertTrue(Files.isRegularFile(input));
        try (XMLSlideShow source = new XMLSlideShow(Files.newInputStream(input));
             XMLSlideShow modified = new XMLSlideShow(Files.newInputStream(output))) {
            assertEquals(10.0, firstRun(source.getSlides().get(1).getShapes().stream()
                .filter(shape -> shape instanceof XSLFTextShape).map(shape -> (XSLFTextShape) shape).findFirst().orElseThrow()).getFontSize());
            var modifiedShapes = modified.getSlides().get(1).getShapes().stream()
                .filter(shape -> shape instanceof XSLFTextShape).map(shape -> (XSLFTextShape) shape).toList();
            assertEquals(24.0, firstRun(modifiedShapes.get(0)).getFontSize());
            assertTrue(firstRun(modifiedShapes.get(0)).isBold());
            assertEquals(10.0, firstRun(modifiedShapes.get(1)).getFontSize());
            assertEquals(10.0, firstRun(modified.getSlides().get(0).getShapes().stream()
                .filter(shape -> shape instanceof XSLFTextShape).map(shape -> (XSLFTextShape) shape).findFirst().orElseThrow()).getFontSize());
            assertEquals(10.0, firstRun(modified.getSlides().get(2).getShapes().stream()
                .filter(shape -> shape instanceof XSLFTextShape).map(shape -> (XSLFTextShape) shape).findFirst().orElseThrow()).getFontSize());
        }
    }

    @Test
    void normalizesNewlinesForDirectoryBatchAndPreservesUppercaseExtensionWithoutOverwrite() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("batch"));
        Path upper = directory.resolve("First.PPTX");
        Path lower = directory.resolve("second.pptx");
        createDeck(upper, List.of(List.of("first\nsecond")));
        createDeck(lower, List.of(List.of("alpha\nbeta")));
        Path nested = Files.createDirectory(directory.resolve("nested"));
        createDeck(nested.resolve("skip.pptx"), List.of(List.of("nested\nfile")));
        FormatterOperations operations = operations(config());

        List<Path> targets = operations.collectFiles(List.of(directory.toString()));
        assertEquals(List.of(upper, lower), targets);

        FormatterOperations.BatchResult result = operations.normalizeNewlines(List.of(directory.toString()), false);

        assertEquals(2, result.successes().size());
        assertTrue(result.failures().isEmpty());
        Path modifiedUpper = directory.resolve("First.mod.PPTX");
        Path modifiedLower = directory.resolve("second.mod.pptx");
        assertTrue(Files.exists(upper));
        assertTrue(Files.exists(lower));
        assertTrue(Files.exists(modifiedUpper));
        assertTrue(Files.exists(modifiedLower));
        try (XMLSlideShow normalized = new XMLSlideShow(Files.newInputStream(modifiedUpper))) {
            XSLFTextShape shape = (XSLFTextShape) normalized.getSlides().get(0).getShapes().get(0);
            assertEquals(2, shape.getTextParagraphs().size());
            assertEquals("first", shape.getTextParagraphs().get(0).getText());
            assertEquals("second", shape.getTextParagraphs().get(1).getText());
        }
    }

    @Test
    void directoryNormalizationBacksUpAllImmediateFilesBeforeOverwritingAndSkipsSubdirectories() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("overwrite-batch"));
        Path first = directory.resolve("first.pptx");
        Path second = directory.resolve("second.pptx");
        createDeck(first, List.of(List.of("first\nline")));
        createDeck(second, List.of(List.of("second\nline")));
        byte[] originalFirst = Files.readAllBytes(first);
        byte[] originalSecond = Files.readAllBytes(second);
        Path nested = Files.createDirectory(directory.resolve("nested"));
        Path skipped = nested.resolve("skipped.pptx");
        createDeck(skipped, List.of(List.of("nested\nline")));
        byte[] skippedBefore = Files.readAllBytes(skipped);
        FormatterOperations operations = operations(config());

        FormatterOperations.DirectoryResult result = operations.normalizeDirectory(directory.toString());

        Path backup = Path.of(result.backupDirectory());
        assertEquals(directory, backup.getParent());
        assertTrue(Files.isDirectory(backup));
        try (var backupFiles = Files.list(backup)) {
            assertEquals(List.of("first.pptx", "second.pptx"), backupFiles
                .map(path -> path.getFileName().toString()).sorted().toList());
        }
        assertArrayEquals(originalFirst, Files.readAllBytes(backup.resolve("first.pptx")));
        assertArrayEquals(originalSecond, Files.readAllBytes(backup.resolve("second.pptx")));
        assertEquals(2, result.result().successes().size());
        assertTrue(result.result().failures().isEmpty());
        assertEquals(first.toAbsolutePath().toString(), result.result().successes().get(0).outputPath());
        try (XMLSlideShow normalized = new XMLSlideShow(Files.newInputStream(first))) {
            XSLFTextShape normalizedShape = (XSLFTextShape) normalized.getSlides().get(0).getShapes().get(0);
            assertEquals(2, normalizedShape.getTextParagraphs().size());
        }
        assertArrayEquals(skippedBefore, Files.readAllBytes(skipped));
    }

    @Test
    void reportsPerFileFailureAndRejectsInvalidInputsBeforeWriting() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("mixed"));
        Path valid = directory.resolve("valid.pptx");
        Path broken = directory.resolve("broken.pptx");
        createDeck(valid, List.of(List.of("target")));
        Files.writeString(broken, "not a presentation");
        FormatterOperations operations = operations(config());

        FormatterOperations.BatchResult result = operations.formatText(
            List.of(directory.toString()), 0, 0, "target", "fontSize: 20", false);

        assertEquals(1, result.successes().size());
        assertEquals(1, result.failures().size());
        assertEquals(broken.toAbsolutePath().toString(), result.failures().get(0).inputPath());
        assertTrue(Files.exists(directory.resolve("valid.mod.pptx")));
        assertFalse(Files.exists(directory.resolve("broken.mod.pptx")));

        assertThrows(IllegalArgumentException.class,
            () -> operations.formatText(List.of(valid.toString()), 2, 1, "target", "fontSize: 20", false));
        assertThrows(IllegalArgumentException.class,
            () -> operations.formatText(List.of(valid.toString()), 0, 0, "[", "fontSize: 20", false));
        assertThrows(IllegalArgumentException.class,
            () -> operations.formatText(List.of(valid.toString()), 0, 0, "target", "fontSize: [", false));
        assertThrows(IllegalArgumentException.class,
            () -> operations.collectFiles(List.of(tempDir.toString())));
    }

    @Test
    void overwriteUsesInputPathAndInvalidFormatDoesNotChangeFile() throws Exception {
        Path input = tempDir.resolve("uppercase.PPTX");
        createDeck(input, List.of(List.of("target")));
        long originalSize = Files.size(input);
        FormatterOperations operations = operations(config());

        assertThrows(IllegalArgumentException.class,
            () -> operations.formatText(List.of(input.toString()), 0, 0, "target", "fontColor: 1000, 0, 0", true));
        assertEquals(originalSize, Files.size(input));

        FormatterOperations.BatchResult result = operations.normalizeNewlines(List.of(input.toString()), true);
        assertEquals(input.toAbsolutePath().toString(), result.successes().get(0).outputPath());
    }

    private FormatterOperations operations(Config config) {
        PowerpointService service = new PowerpointServiceImpl();
        Supplier<Config> configSupplier = () -> config;
        return new FormatterOperations(service, configSupplier, ApplicationExecutionLock.shared());
    }

    private Config config() {
        Config config = new Config();
        config.setPatternPresets(Map.of("target", "target"));
        TextFormat preset = new TextFormat();
        preset.setFontSize(18.0);
        config.setTextFormatPresets(Map.of("preset", preset));
        return config;
    }

    private void createDeck(Path path, List<List<String>> slideTexts) throws Exception {
        try (XMLSlideShow slides = new XMLSlideShow()) {
            for (List<String> texts : slideTexts) {
                var slide = slides.createSlide();
                for (int i = 0; i < texts.size(); i++) {
                    XSLFTextShape shape = slide.createTextBox();
                    shape.setAnchor(new Rectangle(20, 20 + i * 40, 400, 30));
                    shape.setText(texts.get(i));
                    for (XSLFTextRun run : shape.getTextParagraphs().get(0).getTextRuns()) {
                        run.setFontSize(10.0);
                    }
                }
            }
            try (OutputStream output = Files.newOutputStream(path)) {
                slides.write(output);
            }
        }
    }

    private XSLFTextRun firstRun(XSLFTextShape shape) {
        return shape.getTextParagraphs().get(0).getTextRuns().get(0);
    }
}
