package com.scottmo.services.appContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.scottmo.config.definitions.AppConfig;
import com.scottmo.util.LocaleUtil;

@Component("appContextService")
public final class AppContextService {
    private AppConfig appConfig;

    public AppConfig getConfig() {
        if (appConfig == null) {
            reload();
        }
        return appConfig;
    }

    public void reload() {
        Path configPath = Path.of(AppConfig.CONFIG_PATH);
        if (!Files.exists(configPath)) {
            try (InputStream in = AppContextService.class.getClassLoader().getResourceAsStream("config.json")) {
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
        File templateDir = Path.of(appConfig.getDataDir(), AppConfig.TEMPLATE_DIR).toFile();
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

    public String getPPTXTemplate(String fileName) {
        return getRelativePath(Path.of(AppConfig.TEMPLATE_DIR, fileName).toString());
    }

    public String getPrimaryLocale() {
        return appConfig.getLocales().get(0);
    }
}
