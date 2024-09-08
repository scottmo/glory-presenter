package com.scottmo.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // instantiate config
    public void reload() {
        Path configPath = Path.of("./" + Config.CONFIG_FILENAME);
        if (!Files.exists(configPath)) {
            try (InputStream in = ConfigService.class.getClassLoader().getResourceAsStream(Config.CONFIG_FILENAME)) {
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
