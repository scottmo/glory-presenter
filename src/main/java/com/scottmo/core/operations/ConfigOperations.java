package com.scottmo.core.operations;

import java.io.IOException;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.config.Config;
import com.scottmo.config.ConfigService;
import com.scottmo.config.ConfigValidator;

/** Validated, serialized configuration workflows shared by UI and MCP clients. */
public final class ConfigOperations {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ConfigService configService;
    private final ApplicationExecutionLock executionLock;
    private final Consumer<Config> activeConfigApplier;

    public ConfigOperations(ConfigService configService, ApplicationExecutionLock executionLock) {
        this(configService, executionLock, config -> {});
    }

    public ConfigOperations(ConfigService configService, ApplicationExecutionLock executionLock,
        Consumer<Config> activeConfigApplier) {
        this.configService = configService;
        this.executionLock = executionLock;
        this.activeConfigApplier = activeConfigApplier;
    }

    public static ConfigOperations forApplication() {
        return new ConfigOperations(ConfigService.get(), ApplicationExecutionLock.shared(), config -> {
            if (com.scottmo.App.get() != null && !java.awt.GraphicsEnvironment.isHeadless()) {
                com.scottmo.ui.utils.UiConfigurator.applyConfiguration(config);
            }
        });
    }

    public Config snapshot() {
        return executionLock.call(() -> copy(configService.getConfig()));
    }

    public String snapshotJson() {
        return executionLock.call(() -> writeJson(copy(configService.getConfig())));
    }

    public Config saveJson(String json) {
        Config updated = executionLock.call(() -> {
            Config candidate = ConfigValidator.parseAndNormalize(json);
            return copy(configService.install(candidate));
        });
        activeConfigApplier.accept(copy(updated));
        return updated;
    }

    public Config reload() {
        Config updated = executionLock.call(() -> {
            configService.reload();
            return copy(configService.getConfig());
        });
        activeConfigApplier.accept(copy(updated));
        return updated;
    }

    public Config setDarkMode(boolean darkMode) {
        Config updated = executionLock.call(() -> {
            Config candidate = copy(configService.getConfig());
            candidate.setDarkMode(darkMode);
            return copy(configService.install(candidate));
        });
        activeConfigApplier.accept(copy(updated));
        return updated;
    }

    private static Config copy(Config config) {
        try {
            var templatePaths = config.getTemplatePaths() == null ? null : new java.util.LinkedHashSet<>(config.getTemplatePaths());
            Config copy = ConfigValidator.normalizeAndValidate(JSON.readValue(JSON.writeValueAsBytes(config), Config.class));
            if (templatePaths != null) {
                copy.setTemplatePaths(templatePaths);
            }
            return copy;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy application configuration", e);
        }
    }

    private static String writeJson(Config config) {
        try {
            return com.scottmo.shared.JsonUtil.saveToString(config);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize application configuration", e);
        }
    }
}
