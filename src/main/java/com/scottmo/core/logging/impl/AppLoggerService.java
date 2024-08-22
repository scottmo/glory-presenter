package com.scottmo.core.logging.impl;

import org.springframework.stereotype.Component;

import com.scottmo.core.logging.api.AppLogger;

@Component
public class AppLoggerService {
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
