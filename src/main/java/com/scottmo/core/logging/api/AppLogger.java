package com.scottmo.core.logging.api;

public interface AppLogger {
    void info(String msg);
    void warn(String msg);
    void error(String msg, Throwable e);
}
