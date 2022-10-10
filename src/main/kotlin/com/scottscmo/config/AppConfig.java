package com.scottscmo.config;

public class AppConfig {
    private String dataDir;
    private String clientInfoKey;
    private SlideConfig googleSlideConfig;

    public String dataDir() {
        return dataDir;
    }
    public void dataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String clientInfoKey() {
        return clientInfoKey;
    }
    public void clientInfoKey(String clientInfoKey) {
        this.clientInfoKey = clientInfoKey;
    }

    public SlideConfig googleSlideConfig() {
        return googleSlideConfig;
    }
    public void googleSlideConfig(SlideConfig googleSlideConfig) {
        this.googleSlideConfig = googleSlideConfig;
    }
}
