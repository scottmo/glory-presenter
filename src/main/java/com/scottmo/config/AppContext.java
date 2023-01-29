package com.scottmo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.config.definitions.AppConfig;
import com.scottmo.util.LocaleUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppContext {
    public static final String APP_NAME = "Glory Presenter";
    public static final int APP_WIDTH = 900;
    public static final int APP_HEIGHT = 600;

    public static final String CONFIG_PATH = "./config.json";
    private static final String OUTPUT_DIR = "../output"; // same level as data
    private static final String TEMPLATE_DIR = "templates";

    private AppConfig appConfig;

    public AppConfig getConfig() {
        if (appConfig == null) {
            reload();
        }
        if (appConfig.locales().isEmpty()) {
            appConfig.locales().add(LocaleUtil.DEFAULT_LOCALE);
        }
        int i = 0;
        for (String locale: appConfig.locales()) {
            appConfig.locales().set(i++, LocaleUtil.normalize(locale));
        }
        return appConfig;
    }

    public void reload() {
        Path configPath = Path.of(CONFIG_PATH);
        if (!Files.exists(configPath)) {
            try (InputStream in = AppContext.class.getClassLoader().getResourceAsStream("config.json")) {
                Files.copy(in, configPath);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create config.json!");
            }
        }
        try (BufferedReader bufferReader = Files.newBufferedReader(configPath)) {
            appConfig = new ObjectMapper().readValue(bufferReader, AppConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file!", e);
        }
    }

    public String getRelativePath(String fileName) {
        try {
            return Path.of(getConfig().dataDir(), fileName).toFile().getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }

    public String getOutputDir(String fileName) {
        return getRelativePath(Path.of(OUTPUT_DIR, fileName).toString());
    }

    public String getPPTXTemplate(String fileName) {
        return getRelativePath(Path.of(TEMPLATE_DIR, fileName).toString());
    }

    public String getPrimaryLocale() {
        return appConfig.locales().get(0);
    }
}
