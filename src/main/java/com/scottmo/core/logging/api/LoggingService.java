package com.scottmo.core.logging.api;

import com.scottmo.core.Service;

public interface LoggingService extends Service {

    void info(String msg);

    void warn(String msg);

    void error(String msg, Throwable e);
}
