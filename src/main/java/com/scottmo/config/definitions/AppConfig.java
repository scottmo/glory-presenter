package com.scottmo.config.definitions;

public record AppConfig (
        String dataDir,
        String clientInfoKey,
        SlideConfig googleSlideConfig
) {}
