package com.scottmo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    public static final int APP_WIDTH = 900;
    public static final int APP_HEIGHT = 600;
    public static final int UI_GAP = 4;

    public static final String CONFIG_PATH = "./config.json";
    public static final String TEMPLATE_DIR = "templates";

    public static final String LABELS_PATH = "./labels.json";

    private String outputDir;
    private String dataDir;
    private List<String> locales = new ArrayList<>();; // order matters to which locale comes first
    private Map<String, String> bibleVersionToLocale = new HashMap<>();
    private Set<String> templatePaths = new HashSet<>();

    // public AppConfig(
    //         String dataDir,
    //         List<String> locales,
    //         Map<String, String> bibleVersionToLocale,
    //         Set<String> templatePaths) {
    //     this.dataDir = dataDir;
    //     this.locales = (locales == null) ? new ArrayList<>() : locales;
    //     this.bibleVersionToLocale = (bibleVersionToLocale == null) ? new HashMap<>() : bibleVersionToLocale;
    //     this.templatePaths = (templatePaths == null) ? new HashSet<>() : templatePaths;
    // }

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

    public Set<String> getTemplatePaths() {
        return templatePaths;
    }

    public void setTemplatePaths(Set<String> templatePaths) {
        this.templatePaths = templatePaths;
    }
}
