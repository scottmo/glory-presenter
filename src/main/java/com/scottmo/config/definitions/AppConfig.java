package com.scottmo.config.definitions;

import java.util.List;
import java.util.Map;

public record AppConfig (
        String dataDir,
        List<String> locales,
        Map<String, String> bibleVersionToTextConfig
) {}
