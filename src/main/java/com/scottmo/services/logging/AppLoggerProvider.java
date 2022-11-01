package com.scottmo.services.logging;

public class AppLoggerProvider {
    private static AppLogger appLogger;
    public static void registerLogger(AppLogger logger) {
        appLogger = logger;
    }
    public static AppLogger get() {
        return appLogger;
    }
}
