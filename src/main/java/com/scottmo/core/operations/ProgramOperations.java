package com.scottmo.core.operations;

import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.core.ppt.api.PowerpointService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.function.Supplier;

/** Reusable file and generation operations for the Program tab and MCP tools. */
public final class ProgramOperations {
    private static final Pattern PROGRAM_TEMPLATE = Pattern.compile("^program-.*\\.txt$", Pattern.CASE_INSENSITIVE);

    private final Supplier<Path> templateRoot;
    private final Supplier<Path> outputRoot;
    private final PowerpointService powerpointService;

    public ProgramOperations(Path templateRoot, Path outputRoot, PowerpointService powerpointService) {
        Path fixedTemplateRoot = Objects.requireNonNull(templateRoot, "templateRoot").toAbsolutePath().normalize();
        Path fixedOutputRoot = Objects.requireNonNull(outputRoot, "outputRoot").toAbsolutePath().normalize();
        this.templateRoot = () -> fixedTemplateRoot;
        this.outputRoot = () -> fixedOutputRoot;
        this.powerpointService = Objects.requireNonNull(powerpointService, "powerpointService");
    }

    private ProgramOperations(Supplier<Config> config, PowerpointService powerpointService) {
        this.templateRoot = () -> Path.of(config.get().getDataDir(), Config.TEMPLATE_DIR).toAbsolutePath().normalize();
        this.outputRoot = () -> Path.of(config.get().getOutputDir()).toAbsolutePath().normalize();
        this.powerpointService = Objects.requireNonNull(powerpointService, "powerpointService");
    }

    /** Uses the same configured data/templates and output directories as the Swing application. */
    public static ProgramOperations configured(PowerpointService powerpointService) {
        return new ProgramOperations(() -> ConfigService.get().getConfig(), powerpointService);
    }

    public List<String> listTemplates() throws IOException {
        return ApplicationExecutionLock.shared().io(() -> listFiles(false));
    }

    public List<String> listProgramTemplates() throws IOException {
        return ApplicationExecutionLock.shared().io(() -> listFiles(true));
    }

    private List<String> listFiles(boolean programsOnly) throws IOException {
        Path root = existingTemplateRoot();
        try (Stream<Path> files = Files.walk(root)) {
            return files
                .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                .filter(path -> !programsOnly || PROGRAM_TEMPLATE.matcher(path.getFileName().toString()).matches())
                .map(root::relativize)
                .map(ProgramOperations::portablePath)
                .sorted()
                .toList();
        }
    }

    public String readTemplate(String relativePath) throws IOException {
        return ApplicationExecutionLock.shared().io(() -> Files.readString(resolveTemplate(relativePath)));
    }

    public String readProgramTemplate(String relativePath) throws IOException {
        return ApplicationExecutionLock.shared().io(() -> {
            Path path = resolveTemplate(relativePath);
            if (!PROGRAM_TEMPLATE.matcher(path.getFileName().toString()).matches()) {
                throw new IllegalArgumentException("Not a program template: " + relativePath);
            }
            return Files.readString(path);
        });
    }

    /** Generates a merged program deck at output/output.pptx unless a path is supplied. */
    public Path generate(String yamlConfig, String outputPath) throws IOException {
        return ApplicationExecutionLock.shared().io(() -> {
            Objects.requireNonNull(yamlConfig, "yamlConfig");
            Path configuredOutputRoot = outputRoot.get();
            Path output = outputPath == null || outputPath.isBlank()
                ? configuredOutputRoot.resolve("output.pptx")
                : resolveOutputPath(outputPath, configuredOutputRoot);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            powerpointService.generateFromYamlConfigs(yamlConfig, output.toString());
            return output;
        });
    }

    public Path generate(String yamlConfig) throws IOException {
        return generate(yamlConfig, null);
    }

    private Path resolveTemplate(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("Template path is required");
        }
        Path supplied = Path.of(relativePath);
        if (supplied.isAbsolute()) {
            throw new IllegalArgumentException("Template path must be relative to the templates directory");
        }
        Path root = existingTemplateRoot();
        Path candidate = root.resolve(supplied).normalize();
        if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("Template path escapes the templates directory");
        }
        Path realRoot = root.toRealPath();
        Path realCandidate = candidate.toRealPath();
        if (!realCandidate.startsWith(realRoot) || !Files.isRegularFile(realCandidate)) {
            throw new IllegalArgumentException("Template path is not a file inside the templates directory");
        }
        return realCandidate;
    }

    private Path existingTemplateRoot() throws IOException {
        Path root = templateRoot.get().toRealPath();
        if (!Files.isDirectory(root)) {
            throw new IOException("Templates directory is not a directory: " + root);
        }
        return root;
    }

    private static String portablePath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static Path resolveOutputPath(String path, Path outputRoot) {
        Path supplied = Path.of(path);
        return (supplied.isAbsolute() ? supplied : outputRoot.resolve(supplied)).toAbsolutePath().normalize();
    }
}
