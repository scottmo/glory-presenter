package com.scottmo.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.bible.api.bibleMetadata.BookMetadata;
import com.scottmo.shared.LocaleUtil;

public class ConfigService {
    // singleton
    private static ConfigService INSTANCE = new ConfigService();
    public static ConfigService get() {
        return INSTANCE;
    }
    private ConfigService() {}

    // config object
    private Config appConfig;
    public Config getConfig() {
        if (appConfig == null) {
            reload();
        }
        return appConfig;
    }

    private Map<String, Object> labels;
    public String getLabel(String path) {
            if (labels == null) {
                try (InputStream in = ConfigService.class.getClassLoader().getResourceAsStream(Config.LABELS_PATH)){
                ObjectMapper mapper = new ObjectMapper();
                labels = mapper.readValue(in, Map.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load config file!", e);
            }
        }
        Map<String, Object> labelSet = labels;
        String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            if (!labelSet.containsKey(key)) return null;
    
            Object value = labelSet.get(key);
            // if at last key and value is a string, then we have a valid label to return
            if (i + 1 == keys.length && value instanceof String) {
                return (String) value;
            }
            if (value instanceof Map) {
                // if it's a map, let's continue with the path
                labelSet = (Map<String, Object>) value;
            } else {
                // label is invalid
                return null;
            }
        }
        return null;
    }

    // instantiate config
    public void reload() {
        Path configPath = Path.of(Config.CONFIG_PATH);
        if (!Files.exists(configPath)) {
            try (InputStream in = ConfigService.class.getClassLoader().getResourceAsStream("config.json")) {
                Files.copy(in, configPath);
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

    public String getPPTXTemplate(String fileName) {
        return getRelativePath(Path.of(Config.TEMPLATE_DIR, fileName).toString());
    }

    public String getPrimaryLocale() {
        return appConfig.getLocales().get(0);
    }
}
