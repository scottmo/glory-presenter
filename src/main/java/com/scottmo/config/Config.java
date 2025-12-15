package com.scottmo.config;

import com.scottmo.shared.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {

    // default to slightly larger scale since swing was pre-HDPI days and makes things look smaller now
    private static final String UI_SCALE = "1.2";

    public static final int UI_GAP = 8;

    public static final String CONFIG_FILENAME = "config.json";
    public static final String TEMPLATE_DIR = "templates";

    public static final String LABELS_FILENAME = "labels.json";

    private String appDimensions = "1024x768";
    private int appFontSize = 14;
    private String uiScale = UI_SCALE;
    private String outputDir;
    private String dataDir;
    private String uiFontFamily = "Segoe UI, .AppleSystemUIFont, Helvetica Neue, SansSerif";
    private List<String> locales = new ArrayList<>();; // order matters to which locale comes first
    private Map<String, String> bibleVersionToLocale = new HashMap<>();
    private Map<String, String> defaultTemplates = new HashMap<>();
    private Set<String> templatePaths = new HashSet<>();
    private Map<String, TextFormat> textFormatPresets = new HashMap<>();
    private Map<String, String> patternPresets = new HashMap<>();

    public AppSize getAppSize() {
        int width = 1024;
        int height = 768;
        if (appDimensions != null && appDimensions.contains("x")) {
            String[] parts = appDimensions.split("x");
            if (parts.length == 2) {
                try {
                    width = Integer.parseInt(parts[0].trim());
                    height = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    // ignore and use defaults
                }
            }
        }
        return new AppSize(width, height, appFontSize);
    }

    public void setAppSize(AppSize appSize) {
        this.appDimensions = appSize.width() + "x" + appSize.height();
        this.appFontSize = appSize.font();
    }

    public String getAppDimensions() {
        return appDimensions;
    }

    public void setAppDimensions(String appDimensions) {
        this.appDimensions = appDimensions;
    }

    public int getAppFontSize() {
        return appFontSize;
    }

    public void setAppFontSize(int appFontSize) {
        this.appFontSize = appFontSize;
    }

    public String getUiScale() {
        return uiScale;
    }

    public void setUiScale(String uiScale) {
        this.uiScale = uiScale;
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

    public String getUiFontFamily() {
        return uiFontFamily;
    }

    public void setUiFontFamily(String uiFontFamily) {
        this.uiFontFamily = uiFontFamily;
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

    public Map<String, String> getPatternPresets() {
        return patternPresets;
    }

    public void setPatternPresets(Map<String, String> patternPresets) {
        this.patternPresets = patternPresets;
    }

    public record AppSize(int width, int height, int font) {}
}
