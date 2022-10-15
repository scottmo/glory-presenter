package com.scottscmo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class Event {
    public static final String DATA_DIR = "dataDir";

    private static final Map<String, List<Consumer<String>>> handlers = new HashMap<>();
    private static final Map<String, String> lastValues = new HashMap<>();

    public static void emit(String eventName, String value) {
        lastValues.put(eventName, value);
        handlers.get(eventName).forEach(fn -> fn.accept(value));
    }

    public static void subscribe(String eventName, Boolean init, Consumer<String> handler) {
        List<Consumer<String>> listeners = handlers.getOrDefault(eventName, new ArrayList<>());
        listeners.add(handler);
        if (!handlers.containsKey(eventName)) {
            handlers.put(eventName, listeners);
        }

        if (init) {
            String value = lastValues.get(eventName);
            if (value != null) {
                handler.accept(value);
            }
        }
    }

    public static void subscribe(String key, Consumer<String> handler) {
        subscribe(key, false, handler);
    }
}
