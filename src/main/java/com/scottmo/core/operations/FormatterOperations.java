package com.scottmo.core.operations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.shared.Range;
import com.scottmo.shared.TextFormat;

/** Shared PowerPoint formatting workflows for Swing and MCP. */
public final class FormatterOperations {
    private final PowerpointService powerpointService;
    private final Supplier<Config> configSupplier;
    private final ApplicationExecutionLock executionLock;
    private final YAMLMapper yamlMapper = new YAMLMapper();

    public FormatterOperations(
        PowerpointService powerpointService,
        Supplier<Config> configSupplier,
        ApplicationExecutionLock executionLock
    ) {
        this.powerpointService = Objects.requireNonNull(powerpointService, "powerpointService");
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier");
        this.executionLock = Objects.requireNonNull(executionLock, "executionLock");
    }

    public static FormatterOperations forApplication() {
        return new FormatterOperations(
            ServiceProvider.get(PowerpointService.class).get(),
            ConfigService.get()::getConfig,
            ApplicationExecutionLock.shared()
        );
    }

    public List<String> listPatternPresetNames() {
        return executionLock.call(() -> {
            Map<String, String> presets = configSupplier.get().getPatternPresets();
            return presets == null ? List.of() : presets.keySet().stream().sorted().toList();
        });
    }

    /** Collects a single PPTX or immediate PPTX children of each supplied directory. */
    public List<Path> collectFiles(List<String> inputs) {
        return executionLock.call(() -> collectFilesLocked(inputs));
    }

    /** Applies a formatting preset or YAML/JSON format to all matching text runs. */
    public BatchResult formatText(
        List<String> inputs,
        int startSlide,
        int endSlide,
        String matcher,
        String format,
        boolean overwrite
    ) throws IOException {
        return executionLock.io(() -> {
            validateRange(startSlide, endSlide);
            Config config = configSupplier.get();
            Pattern pattern = resolvePattern(matcher, config);
            TextFormat textFormat = resolveTextFormat(format, config);
            List<Path> files = collectFilesLocked(inputs);
            Range range = new Range(startSlide, endSlide);
            return apply(files, overwrite, (input, output) ->
                powerpointService.updateTextFormats(input.toString(), output.toString(), range, pattern, textFormat));
        });
    }

    /** Converts newline characters in each PPTX text shape into separate paragraphs. */
    public BatchResult normalizeNewlines(List<String> inputs, boolean overwrite) throws IOException {
        return executionLock.io(() -> {
            List<Path> files = collectFilesLocked(inputs);
            return apply(files, overwrite, (input, output) ->
                powerpointService.normalizeNewLines(input.toString(), output.toString()));
        });
    }

    private List<Path> collectFilesLocked(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one PowerPoint file or directory.");
        }
        LinkedHashSet<Path> result = new LinkedHashSet<>();
        for (String value : inputs) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("PowerPoint paths must not be blank.");
            }
            Path path = Path.of(value).toAbsolutePath().normalize();
            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.list(path)) {
                    entries.filter(Files::isRegularFile)
                        .filter(FormatterOperations::isPowerPoint)
                        .sorted((left, right) -> left.getFileName().toString().compareToIgnoreCase(right.getFileName().toString()))
                        .forEach(entry -> result.add(entry.toAbsolutePath().normalize()));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Unable to list PowerPoint directory " + path + ": " + message(e), e);
                }
            } else if (Files.isRegularFile(path) && isPowerPoint(path)) {
                result.add(path);
            } else {
                throw new IllegalArgumentException("PowerPoint path is not an existing .pptx file or directory: " + path);
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("No .pptx files were found in the supplied path(s).");
        }
        return List.copyOf(result);
    }

    private Pattern resolvePattern(String matcher, Config config) {
        if (matcher == null || matcher.isBlank()) {
            throw new IllegalArgumentException("A text matcher preset name or regular expression is required.");
        }
        Map<String, String> presets = config.getPatternPresets();
        String value = presets == null ? null : presets.get(matcher.trim());
        if (value == null) {
            value = matcher;
        }
        try {
            return Pattern.compile(value);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid text matcher pattern: " + e.getDescription(), e);
        }
    }

    private TextFormat resolveTextFormat(String format, Config config) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("A text format preset name or YAML/JSON format is required.");
        }
        Map<String, TextFormat> presets = config.getTextFormatPresets();
        TextFormat preset = presets == null ? null : presets.get(format.trim());
        if (preset != null) {
            return preset;
        }
        try {
            TextFormat parsed = yamlMapper.readValue(format, TextFormat.class);
            if (parsed == null) {
                throw new IllegalArgumentException("Text format must describe an object.");
            }
            return parsed;
        } catch (IOException | RuntimeException e) {
            if (e instanceof IllegalArgumentException illegalArgument) {
                throw illegalArgument;
            }
            throw new IllegalArgumentException("Invalid text format YAML/JSON: " + message(e), e);
        }
    }

    private void validateRange(int startSlide, int endSlide) {
        if (startSlide < 0 || endSlide < 0) {
            throw new IllegalArgumentException("Slide range indexes must be zero or greater.");
        }
        if (endSlide != 0 && endSlide <= startSlide) {
            throw new IllegalArgumentException("endSlide must be greater than startSlide; 0 means all slides.");
        }
    }

    private BatchResult apply(List<Path> files, boolean overwrite, FileOperation operation) {
        List<FileSuccess> successes = new ArrayList<>();
        List<FileFailure> failures = new ArrayList<>();
        for (Path input : files) {
            Path output = overwrite ? input : modifiedOutputPath(input);
            try {
                Path parent = output.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                operation.apply(input, output);
                successes.add(new FileSuccess(input.toString(), output.toString()));
            } catch (Exception e) {
                failures.add(new FileFailure(input.toString(), output.toString(), message(e)));
            }
        }
        return new BatchResult(successes, failures);
    }

    private static Path modifiedOutputPath(Path input) {
        String filename = input.getFileName().toString();
        int suffixStart = filename.length() - ".pptx".length();
        String outputName = filename.substring(0, suffixStart) + ".mod" + filename.substring(suffixStart);
        return input.resolveSibling(outputName);
    }

    private static boolean isPowerPoint(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pptx");
    }

    private static String message(Throwable error) {
        String value = error.getMessage();
        return value == null || value.isBlank() ? error.getClass().getSimpleName() : value;
    }

    @FunctionalInterface
    private interface FileOperation {
        void apply(Path input, Path output) throws IOException;
    }

    public record FileSuccess(String inputPath, String outputPath) {}
    public record FileFailure(String inputPath, String outputPath, String error) {}
    public record BatchResult(List<FileSuccess> successes, List<FileFailure> failures) {}
}
