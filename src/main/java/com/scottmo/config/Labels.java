package com.scottmo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Labels {
    private static final String LABEL_MISSING = "MISSING LABEL";
    private static final String LABEL_INVALID = "INVALID LABEL";

    private static Map<String, Object> labels;

    public static String get(String path) {
        if (labels == null) {
            try (InputStream in = Labels.class.getClassLoader().getResourceAsStream(Config.LABELS_FILENAME)){
                ObjectMapper mapper = new ObjectMapper();
                labels = mapper.readValue(in, Map.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load config file!", e);
            }
        }
        Map<String, Object> labelSet = labels;
        String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            if (!labelSet.containsKey(key)) return LABEL_MISSING;
    
            Object value = labelSet.get(key);
            // if at last key and value is a string, then we have a valid label to return
            if (i + 1 == keys.length && value instanceof String) {
                return (String) value;
            }
            if (value instanceof Map) {
                // if it's a map, let's continue with the path
                labelSet = (Map<String, Object>) value;
            } else {
                // label is invalid
                return LABEL_INVALID;
            }
        }
        return LABEL_MISSING;
    }
}
