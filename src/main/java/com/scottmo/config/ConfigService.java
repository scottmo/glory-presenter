package com.scottmo.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.shared.JsonUtil;
import com.scottmo.shared.LocaleUtil;

public class ConfigService {
    // singleton
    private static final ConfigService INSTANCE = new ConfigService(Path.of(Config.CONFIG_FILENAME));
    public static ConfigService get() {
        return INSTANCE;
    }

    private final Path configPath;

    public ConfigService(Path configPath) {
        this.configPath = configPath;
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
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.toAbsolutePath().getParent());
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration directory", e);
            }
            try (InputStream in = ConfigService.class.getClassLoader().getResourceAsStream(Config.CONFIG_FILENAME)) {
                if (in == null) {
                    throw new RuntimeException("Default configuration resource is missing");
                }
                Files.copy(in, configPath);
                System.out.println("Loading default config.json");
            } catch (IOException e) {
                throw new RuntimeException("Unable to create config.json!");
            }
        }
        try (BufferedReader bufferReader = Files.newBufferedReader(configPath)) {
            appConfig = new ObjectMapper().readValue(bufferReader, Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file!", e);
        }
        // set default locale
        if (appConfig.getLocales().isEmpty()) {
            appConfig.getLocales().add(LocaleUtil.DEFAULT_LOCALE);
        }
        // normalize locales
        int i = 0;
        for (String locale: appConfig.getLocales()) {
            appConfig.getLocales().set(i++, LocaleUtil.normalize(locale));
        }
        // load template paths
        File templateDir = Path.of(appConfig.getDataDir(), Config.TEMPLATE_DIR).toFile();
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }
        if (templateDir.isDirectory()) {
            File[] files = templateDir.listFiles();
            for (File file : files) {
                appConfig.getTemplatePaths().add(file.getName());
            }
        } else {
            throw new Error("Unable to load pptx templates! Please fix templates dir and restart!");
        }
        save();
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
        return appConfig.getLocales().get(0);
    }

    public Path getConfigPath() {
        return configPath;
    }

    public synchronized void save() {
        try {
            System.out.println("Saving config.json to " + getConfigPath().toFile().getAbsolutePath());
            JsonUtil.save(getConfigPath().toFile(), appConfig);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save config file!", e);
        }
    }
}
