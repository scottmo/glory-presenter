package com.scottscmo.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static Map<String, String> config = new HashMap<>();
    private static Map<String, List<UpdateListener>> listenersMap = new HashMap<>();

    public final static String DIR_DATA = "dirData";

    static {
        // default config
        config.put(DIR_DATA, Path.of("./data").toAbsolutePath().toString());
    }

    public static String get(String key) {
        return config.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    public static void set(String key, String value) {
        config.put(key, value);
        if (listenersMap.containsKey(key)) {
            listenersMap.get(key).forEach(listener -> {
                listener.handle(value);
            });
        }
    }

    public static void subscribe(String key, UpdateListener handler) {
        List<UpdateListener> listeners = listenersMap.getOrDefault(key, new ArrayList<UpdateListener>());
        listeners.add(handler);
        listenersMap.put(key, listeners);
    }

    public interface UpdateListener {
        public void handle(String value);
    }
}
