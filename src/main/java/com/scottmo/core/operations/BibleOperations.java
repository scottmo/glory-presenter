package com.scottmo.core.operations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleReference.BibleReference;
import com.scottmo.core.operations.ApplicationExecutionLock;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.shared.StringUtils;

/** Workflow operations shared by the desktop UI and MCP tools. */
public final class BibleOperations {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
        "^([0-9]?\\s*[a-zA-Z]+(?:\\s+[a-zA-Z]+)*)\\s+([0-9,;:\\-\\s]+)$"
    );
    private static final Pattern VERSION_PATTERN = Pattern.compile("[A-Za-z0-9_]+(?:-[A-Za-z0-9_]+)*");

    private final BibleService bibleService;
    private final PowerpointService powerpointService;
    private final Supplier<Config> configSupplier;
    private final ApplicationExecutionLock executionLock;

    public BibleOperations() {
        this(
            ServiceProvider.get(BibleService.class).get(),
            ServiceProvider.get(PowerpointService.class).get(),
            ConfigService.get()::getConfig,
            ApplicationExecutionLock.shared()
        );
    }

    public static BibleOperations forApplication() {
        return new BibleOperations(
            ServiceProvider.get(BibleService.class).get(),
            ServiceProvider.get(PowerpointService.class).get(),
            ConfigService.get()::getConfig,
            ApplicationExecutionLock.shared()
        );
    }

    public BibleOperations(
        BibleService bibleService,
        PowerpointService powerpointService,
        Supplier<Config> configSupplier,
        ApplicationExecutionLock executionLock
    ) {
        this.bibleService = Objects.requireNonNull(bibleService, "bibleService");
        this.powerpointService = Objects.requireNonNull(powerpointService, "powerpointService");
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier");
        this.executionLock = Objects.requireNonNull(executionLock, "executionLock");
    }

    public List<String> listVersions() {
        return executionLock.call(bibleService::getAvailableVersions);
    }

    public List<String> listBooks() {
        return executionLock.call(bibleService::getBooks);
    }

    public List<String> listConfiguredTemplates() {
        return executionLock.call(() -> configSupplier.get().getTemplatePaths().stream().sorted().toList());
    }

    public Map<String, String> getBookNames(String bookId) {
        String normalizedBook = normalizeBookId(bookId);
        return executionLock.call(() -> bibleService.getBookNames(normalizedBook));
    }

    public Map<String, List<BibleVerse>> getVerses(String reference, List<String> requestedVersions) {
        return executionLock.call(() -> {
            PreparedReference prepared = prepareReference(reference, requestedVersions);
            return bibleService.getBibleVerses(prepared.reference());
        });
    }

    public ImportResult importOsis(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one OSIS file path.");
        }
        List<String> imported = new ArrayList<>();
        Map<String, String> failures = new LinkedHashMap<>();
        for (String filePath : filePaths) {
            if (filePath == null || filePath.isBlank()) {
                failures.put(String.valueOf(filePath), "File path must not be empty.");
                continue;
            }
            try {
                executionLock.io(() -> {
                    bibleService.importBible(filePath);
                    return null;
                });
                imported.add(filePath);
            } catch (IOException | RuntimeException e) {
                failures.put(filePath, safeMessage(e));
            }
        }
        return new ImportResult(imported, failures);
    }

    public String generatePowerpoint(String bibleReference, String requestedTemplate, String requestedOutputPath)
        throws IOException {
        return executionLock.io(() -> {
            PreparedReference prepared = prepareReference(bibleReference, null);
            String selectedTemplate = resolveTemplate(requestedTemplate);
            String selectedOutput = requestedOutputPath == null || requestedOutputPath.isBlank()
                ? outputDirectory().resolve(StringUtils.sanitizeFilename(bibleReference.trim()) + ".pptx").toString()
                : resolveOutput(requestedOutputPath).toString();
            Path parent = Path.of(selectedOutput).toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            powerpointService.generate(prepared.fullReference(), selectedTemplate, selectedOutput);
            return selectedOutput;
        });
    }

    private String resolveTemplate(String requestedTemplate) {
        String selectedTemplate = requestedTemplate;
        if (selectedTemplate == null || selectedTemplate.isBlank()) {
            Config config = configSupplier.get();
            selectedTemplate = config.getDefaultTemplates().get("bible");
            if (selectedTemplate == null || selectedTemplate.isBlank()) {
                selectedTemplate = config.getTemplatePaths().stream()
                    .filter(path -> path.toLowerCase(Locale.ROOT).contains("bible"))
                    .sorted()
                    .findFirst()
                    .orElse(null);
            }
        }
        if (selectedTemplate == null || selectedTemplate.isBlank()) {
            throw new IllegalArgumentException("No Bible PowerPoint template is configured.");
        }
        Path path = Path.of(selectedTemplate);
        if (!path.isAbsolute()) {
            path = Path.of(configSupplier.get().getDataDir(), Config.TEMPLATE_DIR).resolve(path);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("PowerPoint template does not exist: " + path);
        }
        return path.toAbsolutePath().normalize().toString();
    }

    private PreparedReference prepareReference(String rawReference, List<String> requestedVersions) {
        if (rawReference == null || rawReference.isBlank()) {
            throw new IllegalArgumentException("Bible reference must not be empty.");
        }

        String referenceText = rawReference.trim();
        String versionText = null;
        int versionSeparator = referenceText.indexOf(BibleReference.VERSION_SEPERATOR);
        if (versionSeparator >= 0) {
            if (referenceText.indexOf(BibleReference.VERSION_SEPERATOR,
                    versionSeparator + BibleReference.VERSION_SEPERATOR.length()) >= 0) {
                throw new IllegalArgumentException("Bible reference contains more than one version separator.");
            }
            versionText = referenceText.substring(0, versionSeparator).trim();
            referenceText = referenceText.substring(versionSeparator + BibleReference.VERSION_SEPERATOR.length()).trim();
        }
        if (versionText != null && requestedVersions != null && !requestedVersions.isEmpty()) {
            throw new IllegalArgumentException("Specify Bible versions in the reference or versions argument, not both.");
        }

        Matcher matcher = REFERENCE_PATTERN.matcher(referenceText);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Bible reference. Use a form such as 'john 1:1-3;3:16'.");
        }
        String bookId = normalizeBookId(matcher.group(1));
        String rangesText = matcher.group(2).replaceAll("\\s+", "");
        validateRanges(bookId, rangesText);

        List<String> versions = versionText == null
            ? requestedVersions == null || requestedVersions.isEmpty()
                ? new ArrayList<>(configSupplier.get().getBibleVersionToLocale().keySet())
                : normalizeRequestedVersions(requestedVersions)
            : splitVersions(versionText);
        if (versions.isEmpty()) {
            throw new IllegalArgumentException("No Bible versions are configured or available.");
        }
        validateVersions(versions, bookId);

        String normalizedReference = bookId + " " + rangesText;
        String fullReference = String.join(",", versions) + BibleReference.VERSION_SEPERATOR + normalizedReference;
        try {
            return new PreparedReference(new BibleReference(fullReference), fullReference);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid Bible reference: " + safeMessage(e), e);
        }
    }

    private List<String> splitVersions(String value) {
        List<String> versions = List.of(value.split(",", -1)).stream()
            .map(String::trim)
            .map(version -> version.toLowerCase(Locale.ROOT))
            .toList();
        if (versions.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Bible version names must not be empty.");
        }
        if (new LinkedHashSet<>(versions).size() != versions.size()) {
            throw new IllegalArgumentException("Bible versions must be unique.");
        }
        return versions;
    }

    private List<String> normalizeRequestedVersions(List<String> requestedVersions) {
        if (requestedVersions.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Bible versions must not contain blank values.");
        }
        List<String> versions = requestedVersions.stream()
            .map(String::trim)
            .map(version -> version.toLowerCase(Locale.ROOT))
            .toList();
        if (new LinkedHashSet<>(versions).size() != versions.size()) {
            throw new IllegalArgumentException("Bible versions must be unique.");
        }
        return versions;
    }

    private void validateVersions(List<String> versions, String bookId) {
        Set<String> available = new LinkedHashSet<>(bibleService.getAvailableVersions());
        Map<String, String> names = bibleService.getBookNames(bookId);
        for (String version : versions) {
            if (!VERSION_PATTERN.matcher(version).matches()) {
                throw new IllegalArgumentException("Invalid Bible version name: " + version);
            }
            if (!available.contains(version)) {
                throw new IllegalArgumentException("Unknown Bible version: " + version);
            }
            if (!names.containsKey(version)) {
                throw new IllegalArgumentException("Bible version '%s' does not contain book '%s'.".formatted(version, bookId));
            }
        }
    }

    private String normalizeBookId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bible book must not be empty.");
        }
        String normalized = value.trim().toLowerCase().replaceAll("\\s+", " ");
        if (normalized.equals("psalms")) {
            normalized = "psalm";
        }
        if (normalized.equals("proverb")) {
            normalized = "proverbs";
        }
        if (!BibleMetadata.getBookInfoMap().containsKey(normalized)) {
            throw new IllegalArgumentException("Unknown Bible book: " + value);
        }
        return normalized;
    }

    private void validateRanges(String bookId, String rangesText) {
        var metadata = BibleMetadata.getBookInfoMap().get(bookId);
        for (String part : rangesText.split(";", -1)) {
            if (part.isBlank()) {
                throw new IllegalArgumentException("Bible reference contains an empty chapter range.");
            }
            String[] chapterAndVerses = part.split(":", -1);
            if (chapterAndVerses.length > 2 || chapterAndVerses[0].isBlank()) {
                throw new IllegalArgumentException("Invalid chapter range: " + part);
            }
            int chapter = parsePositiveInt(chapterAndVerses[0], "chapter");
            if (chapter > metadata.count().size()) {
                throw new IllegalArgumentException("Chapter %d does not exist in %s.".formatted(chapter, bookId));
            }
            if (chapterAndVerses.length == 1) {
                continue;
            }
            if (chapterAndVerses[1].isBlank()) {
                throw new IllegalArgumentException("Verse selection must not be empty: " + part);
            }
            int maxVerse = metadata.count().get(chapter - 1);
            for (String versePart : chapterAndVerses[1].split(",", -1)) {
                if (versePart.isBlank()) {
                    throw new IllegalArgumentException("Bible reference contains an empty verse.");
                }
                String[] endpoints = versePart.split("-", -1);
                if (endpoints.length > 2) {
                    throw new IllegalArgumentException("Invalid verse range: " + versePart);
                }
                int first = parsePositiveInt(endpoints[0], "verse");
                int last = endpoints.length == 1 ? first : parsePositiveInt(endpoints[1], "verse");
                if (first > last) {
                    throw new IllegalArgumentException("Invalid verse range %s: end must not precede start.".formatted(versePart));
                }
                if (last > maxVerse) {
                    throw new IllegalArgumentException("Verse %d does not exist in %s %d.".formatted(last, bookId, chapter));
                }
            }
        }
    }

    private int parsePositiveInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new IllegalArgumentException(label + " must be greater than zero.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value, e);
        }
    }

    private String safeMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    private Path outputDirectory() {
        String configured = configSupplier.get().getOutputDir();
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Application outputDir is not configured");
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private Path resolveOutput(String outputPath) {
        Path requested = Path.of(outputPath);
        return (requested.isAbsolute() ? requested : outputDirectory().resolve(requested))
            .toAbsolutePath().normalize();
    }

    public record ImportResult(List<String> imported, Map<String, String> failures) {}

    private record PreparedReference(BibleReference reference, String fullReference) {}
}
