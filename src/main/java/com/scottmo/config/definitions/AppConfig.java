package com.scottmo.config.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AppConfig (
        String dataDir,
        List<String> locales,
        Map<String, String> bibleVersionToLocale,
        Set<String> templatePaths
) {
    public AppConfig {
        if (locales == null) locales = new ArrayList<>();
        if (bibleVersionToLocale == null) bibleVersionToLocale = new HashMap<>();
        if (templatePaths == null) templatePaths = new HashSet<>();
    }
}
