package com.scottmo.services.logging;

public class AppLoggerService implements Service {
    private AppLogger appLogger;
    public void registerLogger(AppLogger logger) {
        appLogger = logger;
    }
    public AppLogger get() {
        return appLogger;
    }
}
