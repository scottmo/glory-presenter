package com.scottmo.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ErrorRegistry {
    private static final List<String> logs = new ArrayList<>();
    private static Consumer<String> listener;

    public static synchronized void registerListener(Consumer<String> logListener) {
        listener = logListener;
        // Deliver existing logs to the new listener
        for (String log : logs) {
            logListener.accept(log);
        }
    }

    public static synchronized void unregisterListener() {
        listener = null;
    }

    public static synchronized void log(String logEntry) {
        logs.add(logEntry);
        if (listener != null) {
            listener.accept(logEntry);
        }
    }

    public static synchronized void clear() {
        logs.clear();
    }

    public static synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }
}
