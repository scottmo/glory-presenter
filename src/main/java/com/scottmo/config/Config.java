package com.scottmo.config;

import com.scottmo.shared.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    public static final AppSize COMPACT_SIZE = new AppSize(900, 600, 14);
    public static final AppSize COMFORT_SIZE = new AppSize(1100, 800, 16);

    public static final int UI_GAP = 8;

    public static final String CONFIG_FILENAME = "config.json";
    public static final String TEMPLATE_DIR = "templates";

    public static final String LABELS_FILENAME = "labels.json";

    private AppSize appSize = COMPACT_SIZE;
    private String outputDir;
    private String dataDir;
    private List<String> locales = new ArrayList<>();; // order matters to which locale comes first
    private Map<String, String> bibleVersionToLocale = new HashMap<>();
    private Map<String, String> defaultTemplates = new HashMap<>();
    private Set<String> templatePaths = new HashSet<>();
    private Map<String, TextFormat> textFormatPresets = new HashMap<>();

    public AppSize getAppSize() {
        return appSize;
    }

    public void setAppSize(AppSize appSize) {
        this.appSize = appSize;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public List<String> getLocales() {
        return locales;
    }

    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    public Map<String, String> getBibleVersionToLocale() {
        return bibleVersionToLocale;
    }

    public void setBibleVersionToLocale(Map<String, String> bibleVersionToLocale) {
        this.bibleVersionToLocale = bibleVersionToLocale;
    }

    public Map<String, String> getDefaultTemplates() {
        return defaultTemplates;
    }

    public void setDefaultTemplates(Map<String, String> defaultTemplates) {
        this.defaultTemplates = defaultTemplates;
    }

    public Set<String> getTemplatePaths() {
        return templatePaths;
    }

    public void setTemplatePaths(Set<String> templatePaths) {
        this.templatePaths = templatePaths;
    }

    public Map<String, TextFormat> getTextFormatPresets() {
        return textFormatPresets;
    }

    public void setTextFormatPresets(Map<String, TextFormat> textFormatPresets) {
        this.textFormatPresets = textFormatPresets;
    }

    public record AppSize(int width, int height, int font) {}
}
