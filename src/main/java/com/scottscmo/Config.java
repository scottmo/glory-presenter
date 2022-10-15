package com.scottscmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottscmo.config.AppConfig;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    public static final String SONG_DIR = "songs";
    public static final String SONG_SLIDES_DIR = "songs_slide";

    public static final String GOOGLE_API_DIR = "google_api";
    public static final String GOOGLE_API_CREDENTIALS_PATH = GOOGLE_API_DIR + "/client.info";

    public static final String CONFIG_PATH = "./config.json";

    private static final String OUTPUT_DIR = "../output"; // same level as data

    public static Font getTextAreaFont() {
        return new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }

    private static AppConfig _config;

    public static AppConfig get() {
        if (_config == null) {
            reload();
        }
        return _config;
    }

    public static void reload() {
        try (BufferedReader bufferReader = Files.newBufferedReader((Path.of(CONFIG_PATH)))) {
            _config = new ObjectMapper().readValue(bufferReader, AppConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file!", e);
        }
    }

    public static String getRelativePath(String fileName) {
        try {
            return Path.of(get().dataDir(), fileName).toFile().getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }

    public static String getOutputDir(String fileName) {
        return getRelativePath(Path.of(OUTPUT_DIR, fileName).toString());
    }
}
