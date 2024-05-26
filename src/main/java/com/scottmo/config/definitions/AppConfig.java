package com.scottmo.config.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppConfig {
    private String dataDir;
    private List<String> locales;
    private Map<String, String> bibleVersionToLocale;
    private Set<String> templatePaths;

    public AppConfig(
            String dataDir,
            List<String> locales,
            Map<String, String> bibleVersionToLocale,
            Set<String> templatePaths) {
        this.dataDir = dataDir;
        this.locales = (locales == null) ? new ArrayList<>() : locales;
        this.bibleVersionToLocale = (bibleVersionToLocale == null) ? new HashMap<>() : bibleVersionToLocale;
        this.templatePaths = (templatePaths == null) ? new HashSet<>() : templatePaths;
    }

    public String dataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public List<String> locales() {
        return locales;
    }

    public void setLocales(List<String> locales) {
        this.locales = locales;
    }

    public Map<String, String> bibleVersionToLocale() {
        return bibleVersionToLocale;
    }

    public void setBibleVersionToLocale(Map<String, String> bibleVersionToLocale) {
        this.bibleVersionToLocale = bibleVersionToLocale;
    }

    public Set<String> templatePaths() {
        return templatePaths;
    }

    public void setTemplatePaths(Set<String> templatePaths) {
        this.templatePaths = templatePaths;
    }
}
