package config;

public record AppConfig(
        String dataDir,
        String clientInfoKey,
        SlideConfig googleSlideConfig
) {}
