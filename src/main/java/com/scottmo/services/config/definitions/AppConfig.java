package com.scottmo.services.config.definitions;

public record AppConfig (
        String dataDir,
        String clientInfoKey,
        SlideConfig googleSlideConfig
) {}
