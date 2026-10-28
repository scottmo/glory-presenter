package com.scottmo.config;

import java.awt.Color;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.shared.LocaleUtil;
import com.scottmo.shared.TextFormat;

/** Validates configuration values before they are persisted or activated. */
public final class ConfigValidator {
    private static final Pattern DIMENSIONS = Pattern.compile("([1-9][0-9]{0,4})x([1-9][0-9]{0,4})");
    private static final Pattern LOCALE = Pattern.compile("[a-z]{2,8}(?:_[a-z0-9]{2,8})*");
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private ConfigValidator() {}

    public static Config parseAndNormalize(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Configuration JSON is required");
        }
        try {
            return normalizeAndValidate(MAPPER.readValue(json, Config.class));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration JSON: " + e.getMessage(), e);
        }
    }

    public static Config normalizeAndValidate(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration object is required");
        }
        if (config.getAppDimensions() == null || !DIMENSIONS.matcher(config.getAppDimensions().trim()).matches()) {
            throw new IllegalArgumentException("appDimensions must be WIDTHxHEIGHT with positive dimensions");
        }
        if (config.getAppFontSize() < 1 || config.getAppFontSize() > 512) {
            throw new IllegalArgumentException("appFontSize must be between 1 and 512");
        }
        config.setUiScale(normalizeUiScale(config.getUiScale()));
        config.setDataDir(normalizePath("dataDir", config.getDataDir()).toString());
        config.setOutputDir(normalizePath("outputDir", config.getOutputDir()).toString());
        if (config.getLocales() == null) {
            throw new IllegalArgumentException("locales must be an array");
        }
        if (config.getLocales().isEmpty()) {
            config.getLocales().add(LocaleUtil.DEFAULT_LOCALE);
        }
        LinkedHashSet<String> locales = new LinkedHashSet<>();
        for (String locale : config.getLocales()) {
            if (locale == null || locale.isBlank()) {
                throw new IllegalArgumentException("locales must not contain blank values");
            }
            String normalized = LocaleUtil.normalize(locale);
            if (!LOCALE.matcher(normalized).matches() || !locales.add(normalized)) {
                throw new IllegalArgumentException("locales must contain unique locale codes: " + locale);
            }
        }
        config.setLocales(new ArrayList<>(locales));

        if (config.getBibleVersionToLocale() == null || config.getDefaultTemplates() == null
            || config.getPatternPresets() == null || config.getTextFormatPresets() == null) {
            throw new IllegalArgumentException("Configuration preset maps must not be null");
        }
        Map<String, String> bibleLocales = new LinkedHashMap<>();
        config.getBibleVersionToLocale().forEach((version, locale) -> {
            if (version == null || version.isBlank() || locale == null || !LOCALE.matcher(LocaleUtil.normalize(locale)).matches()) {
                throw new IllegalArgumentException("bibleVersionToLocale entries need a version and locale");
            }
            bibleLocales.put(version.trim().toLowerCase(Locale.ROOT), LocaleUtil.normalize(locale));
        });
        config.setBibleVersionToLocale(bibleLocales);
        config.getDefaultTemplates().forEach((name, path) -> {
            if (name == null || name.isBlank() || path == null || path.isBlank()) {
                throw new IllegalArgumentException("defaultTemplates entries need a name and path");
            }
            try {
                Path.of(path.trim());
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException("Invalid default template path for " + name, e);
            }
        });
        config.getPatternPresets().forEach((name, expression) -> {
            if (name == null || name.isBlank() || expression == null) {
                throw new IllegalArgumentException("patternPresets entries need a name and expression string");
            }
            try {
                Pattern.compile(expression);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid pattern preset '" + name + "': " + e.getDescription(), e);
            }
        });
        config.getTextFormatPresets().forEach((name, format) -> validateTextFormat(name, format));
        config.setTemplatePaths(new LinkedHashSet<>());
        return config;
    }

    private static Path normalizePath(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        try {
            Path normalized = Path.of(value.trim()).normalize();
            return normalized.toString().isEmpty() ? Path.of(".") : normalized;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid " + name + " path", e);
        }
    }

    private static String normalizeUiScale(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("uiScale must be a positive scale such as 1.5, 150%, or 144dpi");
        }
        String normalized = value.trim();
        java.util.regex.Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(x|%|dpi)?", Pattern.CASE_INSENSITIVE)
            .matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("uiScale must be a positive scale such as 1.5, 150%, or 144dpi");
        }
        try {
            double number = Double.parseDouble(matcher.group(1));
            if (!Double.isFinite(number) || number <= 0) {
                throw new IllegalArgumentException("uiScale must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("uiScale must be a positive scale such as 1.5, 150%, or 144dpi", e);
        }
        return normalized;
    }

    private static void validateTextFormat(String name, TextFormat format) {
        if (name == null || name.isBlank() || format == null) {
            throw new IllegalArgumentException("textFormatPresets entries need a name and format object");
        }
        Double size = format.getFontSize();
        if (size != null && (!Double.isFinite(size) || size <= 0 || size > 512)) {
            throw new IllegalArgumentException("textFormatPresets." + name + ".fontSize must be between 0 and 512");
        }
        String color = format.getFontColor();
        if (color != null) {
            String[] channels = color.split(",");
            if (channels.length != 3) {
                throw new IllegalArgumentException("textFormatPresets." + name + ".fontColor must have three channels");
            }
            try {
                int red = Integer.parseInt(channels[0].trim());
                int green = Integer.parseInt(channels[1].trim());
                int blue = Integer.parseInt(channels[2].trim());
                new Color(red, green, blue);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("textFormatPresets." + name + ".fontColor channels must be 0-255", e);
            }
        }
    }
}
