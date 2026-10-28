package com.scottmo.core.operations;

import com.scottmo.ui.utils.ErrorRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/** Read, clear, and export operations over the application's existing in-memory log. */
public final class LogOperations {
    private final Path defaultExportPath;

    public LogOperations() {
        this(Path.of("error.log"));
    }

    public LogOperations(Path defaultExportPath) {
        this.defaultExportPath = Objects.requireNonNull(defaultExportPath, "defaultExportPath")
            .toAbsolutePath().normalize();
    }

    /** Returns the current log snapshot, optionally limited to the most recent entries. */
    public List<String> read(Integer limit) {
        return ApplicationExecutionLock.shared().call(() -> {
            if (limit != null && limit < 1) {
                throw new IllegalArgumentException("limit must be at least 1");
            }
            List<String> logs = ErrorRegistry.getLogs();
            if (limit == null || logs.size() <= limit) {
                return logs;
            }
            return List.copyOf(logs.subList(logs.size() - limit, logs.size()));
        });
    }

    /** Clears the in-memory log and notifies every registered view. Returns the number of entries cleared. */
    public int clear() {
        return ApplicationExecutionLock.shared().call(ErrorRegistry::clearAndCount);
    }

    /** Exports the current in-memory snapshot as UTF-8, defaulting to the UI's error.log path. */
    public Path export(String outputPath) throws IOException {
        return ApplicationExecutionLock.shared().io(() -> {
            List<String> logs = ErrorRegistry.getLogs();
            Path output = outputPath == null || outputPath.isBlank()
                ? defaultExportPath
                : Path.of(outputPath).toAbsolutePath().normalize();
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, String.join("", logs), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return output;
        });
    }

    public Path export() throws IOException {
        return export(null);
    }
}
