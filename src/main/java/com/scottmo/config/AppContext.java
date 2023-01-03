package com.scottmo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.config.definitions.AppConfig;
import com.scottmo.util.LocaleUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppContext {
    public static final String APP_NAME = "Glory Presenter";

    public static final int APP_WIDTH = 900;
    public static final int APP_HEIGHT = 600;

    // TODO: make this configurable
    public static final String PRIMARY_LOCALE = LocaleUtil.DEFAULT_LOCALE;
    public static final String SECONDARY_LOCALE = LocaleUtil.normalize("zh_CN");

    public static final String CONTENTS_DIR = "contents";
    public static final String CONTENTS_SLIDE_DIR = "contents_slide";

    public static final String GOOGLE_API_DIR = "google_api";
    public static final String GOOGLE_API_CREDENTIALS_PATH = GOOGLE_API_DIR + "/client.info";

    public static final String CONFIG_PATH = "./config.json";

    private static final String OUTPUT_DIR = "../output"; // same level as data

    private AppConfig _config;

    public AppConfig getConfig() {
        if (_config == null) {
            reload();
        }
        return _config;
    }

    public void reload() {
        try (BufferedReader bufferReader = Files.newBufferedReader((Path.of(CONFIG_PATH)))) {
            _config = new ObjectMapper().readValue(bufferReader, AppConfig.class);
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
}
