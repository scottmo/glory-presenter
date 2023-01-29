package com.scottmo.services.logging;

import com.scottmo.services.Service;

public class AppLoggerService implements Service {
    private AppLogger appLogger;
    public void registerLogger(AppLogger logger) {
        appLogger = logger;
    }
    public void info(String msg) {
        appLogger.info(msg);
    }
    public void warn(String msg) {
        appLogger.warn(msg);
    }
    public void error(String msg, Throwable e) {
        appLogger.error(msg, e);
    }
}
