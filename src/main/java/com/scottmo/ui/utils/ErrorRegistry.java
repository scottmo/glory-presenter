package com.scottmo.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ErrorRegistry {
    private static final List<String> logs = new ArrayList<>();
    private static final List<Listener> listeners = new ArrayList<>();

    public static synchronized void registerListener(Consumer<String> logListener) {
        registerListener(logListener, () -> {});
    }

    /** Replaces the legacy listener with log and clear callbacks. */
    public static synchronized void registerListener(Consumer<String> logListener, Runnable clearListener) {
        listeners.clear();
        addListener(logListener, clearListener);
    }

    /** Adds an independently removable listener for views that coexist. */
    public static synchronized Registration addListener(Consumer<String> logListener, Runnable clearListener) {
        Listener listener = new Listener(Objects.requireNonNull(logListener, "logListener"),
            Objects.requireNonNull(clearListener, "clearListener"));
        listeners.add(listener);
        for (String log : logs) {
            logListener.accept(log);
        }
        return () -> removeListener(listener);
    }

    public static synchronized void unregisterListener() {
        listeners.clear();
    }

    public static synchronized void log(String logEntry) {
        logs.add(logEntry);
        for (Listener listener : List.copyOf(listeners)) {
            listener.onLog().accept(logEntry);
        }
    }

    public static synchronized void clear() {
        clearAndCount();
    }

    /** Clears the in-memory log and reports the number of entries removed atomically. */
    public static synchronized int clearAndCount() {
        int count = logs.size();
        logs.clear();
        for (Listener listener : List.copyOf(listeners)) {
            listener.onClear().run();
        }
        return count;
    }

    public static synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    private static synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private record Listener(Consumer<String> onLog, Runnable onClear) {}

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        @Override
        void close();
    }
}
