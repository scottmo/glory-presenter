package com.scottscmo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class KVMD {
    private static final String KEY_NAMESPACE = "namespace";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_CONTENT = "content";

    private static final String SECTION_DELIMITER = "===";

    private static final String ERR_MSG_INVALID_FORMAT = "KVMD.parse: Invalid input format!";

    public static String getNamespace(Map<String, Object> obj) {
        return (String) obj.get(KEY_NAMESPACE);
    }

    public static Map<String, Object> getMetadata(Map<String, Object> obj) {
        return (Map<String, Object>) obj.getOrDefault(KEY_METADATA, new HashMap<>());
    }

    public static Map<String, Object> getContent(Map<String, Object> obj) {
        return (Map<String, Object>) obj.getOrDefault(KEY_CONTENT, new HashMap<>());
    }

    public static Map<String, Object> create(String namespace, Map<String, Object> metadata, Map<String, Object> content) {
        return Map.of(
                KEY_NAMESPACE, namespace,
                KEY_METADATA, metadata,
                KEY_CONTENT, content);
    }

    public static Map<String, Object> parse(String input) {
        String namespace;
        Map<String, Object> metadata;
        Map<String, Object> content;

        List<String> sections = Arrays.stream(input.split(SECTION_DELIMITER)).map(String::trim).toList();
        assert sections.size() <= 3 : ERR_MSG_INVALID_FORMAT + " Too many sections";

        switch (sections.size()) {
            case 1 -> {
                namespace = "";
                metadata = Collections.emptyMap();
                content = parseContent(sections.get(0));
            }
            case 2 -> {
                namespace = "";
                metadata = parseMetadata(sections.get(0));
                content = parseContent(sections.get(1));
            }
            default -> {
                namespace = sections.get(0);
                metadata = parseMetadata(sections.get(1));
                content = parseContent(sections.get(2));
            }
        }

        return create(namespace, metadata, content);
    }

    private static Map<String, Object> parseMetadata(String input) {
        return Arrays.stream(input.split("\n"))
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(arr -> arr[0].trim(), arr -> parseMetadataValue(arr[1].trim())));
    }

    private static Object parseMetadataValue(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { // array
            return Arrays.stream(value.substring(1, value.length() - 1).split(",")).map(String::trim).toList();
        }
        return value; // string
    }

    /**
     * Recursively parse content. Throw if level is missing.
     */
    private static Map<String, Object> parseContent(String input, String prefix, int level) {
        // make sure we have the prefix at the very least
        Pattern keyRegex = Pattern.compile("(^|\n)" + prefix);
        if (!keyRegex.matcher(input).find()) return Collections.emptyMap();

        // find all the keys and recursively parse the values
        Pattern currentLevelKeyRegex = Pattern.compile("(^|\n)" + prefix + "{" + level + "}\\s(.+)($|\n)");
        Matcher currentLevelKeyMatcher = currentLevelKeyRegex.matcher(input);

        List<String> keys = new ArrayList<>();
        while (currentLevelKeyMatcher.find()) {
            keys.add(currentLevelKeyMatcher.group(2));
        }

        assert !keys.isEmpty() : ERR_MSG_INVALID_FORMAT + " Missing level %d for prefix %s".formatted(level, prefix);

        List<Object> values = Arrays.stream(currentLevelKeyRegex.split(input))
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .map(value -> {
                    Map<String, Object> content = parseContent(value, prefix, level + 1);
                    return content.isEmpty() ? value : content;
                })
                .toList();
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, values::get));
    }

    private static Map<String, Object> parseContent(String input) {
        return parseContent(input, "#", 1);
    }

    /**
     * Serialize object to KVMD format. Should be in the following structure:
     * {
     *     metadata: {
     *         [key: String]: String | Array
     *     },
     *     content: {
     *         [key: String]: String | Map
     *     }
     * }
     */
    public static String stringify(Map<String, Object> obj) {
        String namespace = getNamespace(obj);
        String metadataStr = getMetadata(obj).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ": " + stringifyMetadataValue(entry.getValue()))
                .collect(Collectors.joining("\n"));
        String contentStr = stringifyContent(getContent(obj));

        return Stream.of(namespace, metadataStr, contentStr)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n$SECTION_DELIMITER\n")) + "\n";
    }

    private static String stringifyMetadataValue(Object value) {
        if (value instanceof List) {
            return "[%s]".formatted(String.join(", ", ((List<String>) value)));
        }
        return value.toString();
    }

    private static String stringifyContent(Map<String, Object> content, String prefix, int level) {
        return content.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String key = "\n" + prefix.repeat(level) + " " + entry.getKey();
                    if (entry.getValue() instanceof Map) {
                        return key + "\n" + stringifyContent((Map<String, Object>) entry.getValue(), prefix, level + 1);
                    } else {
                        return key + "\n" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("\n"));
    }

    private static String stringifyContent(Map<String, Object> content) {
        return stringifyContent(content, "#", 1);
    }
}