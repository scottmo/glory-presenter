package com.scottmo.core.logging.api;

public interface AppLoggerService {

    void registerLogger(AppLogger logger);

    void info(String msg);

    void warn(String msg);

    void error(String msg, Throwable e);

}
