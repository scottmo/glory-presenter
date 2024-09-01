package com.scottmo.core.logging.impl;

import com.scottmo.core.logging.api.AppLogger;
import com.scottmo.core.logging.api.AppLoggerService;

public class AppLoggerServiceImpl implements AppLoggerService {
    private AppLogger appLogger;
    @Override
    public void registerLogger(AppLogger logger) {
        appLogger = logger;
    }
    @Override
    public void info(String msg) {
        appLogger.info(msg);
    }
    @Override
    public void warn(String msg) {
        appLogger.warn(msg);
    }
    @Override
    public void error(String msg, Throwable e) {
        appLogger.error(msg, e);
    }
}
