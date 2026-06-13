package com.scottmo.ui.utils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class InMemoryAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {
        String message = (this.layout != null) ? this.layout.format(event) : event.getRenderedMessage() + "\n";
        ErrorRegistry.log(message);

        if (this.layout != null && this.layout.ignoresThrowable()) {
            String[] s = event.getThrowableStrRep();
            if (s != null) {
                for (String value : s) {
                    ErrorRegistry.log(value + "\n");
                }
            }
        }
    }

    @Override
    public void close() {}

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
