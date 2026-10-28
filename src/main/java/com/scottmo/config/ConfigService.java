package com.scottmo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.Objects;

import com.scottmo.shared.JsonUtil;

public class ConfigService {
    // singleton
    private static final ConfigService INSTANCE = new ConfigService(Path.of(Config.CONFIG_FILENAME));
    public static ConfigService get() {
        return INSTANCE;
    }

    private final Path configPath;

    public ConfigService(Path configPath) {
        this.configPath = Objects.requireNonNull(configPath, "configPath");
    }

    // config object
    private volatile Config appConfig;
    public synchronized Config getConfig() {
        if (appConfig == null) {
            reload();
        }
        return appConfig;
    }

    // instantiate config
    public synchronized void reload() {
        Config candidate = parseConfig(readConfigContent());
        prepareDirectories(candidate);
        refreshTemplatePaths(candidate);
        appConfig = candidate;
    }

    /** Validate and atomically persist a replacement, activating it only after the write succeeds. */
    public synchronized Config install(Config candidate) {
        Config normalized = ConfigValidator.normalizeAndValidate(candidate);
        prepareDirectories(normalized);
        refreshTemplatePaths(normalized);
        persist(normalized);
        appConfig = normalized;
        return normalized;
    }

    private Config parseConfig(String json) {
        return ConfigValidator.parseAndNormalize(json);
    }

    private String readConfigContent() {
        try {
            if (!Files.exists(configPath)) {
                try (InputStream in = ConfigService.class.getClassLoader().getResourceAsStream(Config.CONFIG_FILENAME)) {
                    if (in == null) {
                        throw new IOException("Default configuration resource is missing");
                    }
                    String defaults = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    Config parsed = parseConfig(defaults);
                    prepareDirectories(parsed);
                    persist(parsed);
                    System.out.println("Loading default config.json");
                }
            }
            return Files.readString(configPath, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read configuration file " + configPath, e);
        }
    }

    private static void prepareDirectories(Config config) {
        try {
            Files.createDirectories(Path.of(config.getDataDir()));
            Path templates = Path.of(config.getDataDir(), Config.TEMPLATE_DIR);
            Files.createDirectories(templates);
            if (!Files.isDirectory(templates)) {
                throw new IOException("Template path is not a directory: " + templates);
            }
            Files.createDirectories(Path.of(config.getOutputDir()));
            if (!Files.isDirectory(Path.of(config.getOutputDir()))) {
                throw new IOException("Output path is not a directory: " + config.getOutputDir());
            }
        } catch (IOException | SecurityException e) {
            throw new IllegalArgumentException("Unable to prepare configured directories: " + e.getMessage(), e);
        }
    }

    private static void refreshTemplatePaths(Config config) {
        File[] files = Path.of(config.getDataDir(), Config.TEMPLATE_DIR).toFile().listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Unable to list templates directory");
        }
        config.getTemplatePaths().clear();
        for (File file : files) {
            config.getTemplatePaths().add(file.getName());
        }
    }

    private void persist(Config config) {
        Path parent = configPath.toAbsolutePath().normalize().getParent();
        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, configPath.getFileName().toString(), ".tmp");
            Files.writeString(temporary, JsonUtil.saveToString(config), java.nio.charset.StandardCharsets.UTF_8);
            try {
                Files.move(temporary, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save configuration file " + configPath, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Best-effort cleanup after an atomic replace.
                }
            }
        }
    }

    public String getRelativePath(String fileName) {
        try {
            return Path.of(getConfig().getDataDir(), fileName).toFile().getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }

    public String getOutputPath(String fileName) {
        try {
            return Path.of(getConfig().getOutputDir(), fileName).toFile().getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }

    public String getPowerpointTemplate(String fileName) {
        return getRelativePath(Path.of(Config.TEMPLATE_DIR, fileName).toString());
    }

    public String getPrimaryLocale() {
        return getConfig().getLocales().get(0);
    }

    public Path getConfigPath() {
        return configPath;
    }

    public synchronized void save() {
        Config candidate;
        try {
            candidate = parseConfig(JsonUtil.saveToString(getConfig()));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize application configuration", e);
        }
        prepareDirectories(candidate);
        refreshTemplatePaths(candidate);
        persist(candidate);
        appConfig = candidate;
    }
}
