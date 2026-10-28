package com.scottmo.mcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;
import io.modelcontextprotocol.spec.McpSchema;

/** Shared JSON Schema, input validation, serialization, and expected-error handling for tools. */
public final class ToolSupport {
    private static final Logger LOGGER = Logger.getLogger(ToolSupport.class);

    private ToolSupport() {}

    public static Map<String, Object> objectSchema(Map<String, Object> properties, String... required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        if (required.length > 0) {
            schema.put("required", Arrays.asList(required));
        }
        return schema;
    }

    public static Map<String, Object> stringSchema(String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        return schema;
    }

    public static Map<String, Object> integerSchema(String description, int minimum, int maximum) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "integer");
        schema.put("minimum", minimum);
        schema.put("maximum", maximum);
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        return schema;
    }

    public static Map<String, Object> arraySchema(Map<String, Object> items, String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        schema.put("items", items);
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        return schema;
    }

    public static McpSchema.Tool tool(String name, String description, Map<String, Object> inputSchema,
        ToolAnnotations annotations) {
        return McpSchema.Tool.builder()
            .name(name)
            .description(description)
            .inputSchema(inputSchema)
            .annotations(annotations)
            .build();
    }

    public static ToolAnnotations annotations(String title, boolean readOnly, boolean destructive, boolean idempotent) {
        return ToolAnnotations.builder()
            .title(title)
            .readOnlyHint(readOnly)
            .destructiveHint(destructive)
            .idempotentHint(idempotent)
            .build();
    }

    public static String requiredString(Map<String, Object> arguments, String name) {
        String value = optionalString(arguments, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("'" + name + "' is required and must not be blank");
        }
        return value;
    }

    public static String optionalString(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String string)) {
            throw new IllegalArgumentException("'" + name + "' must be a string");
        }
        return string;
    }

    public static int requiredInt(Map<String, Object> arguments, String name, int minimum, int maximum) {
        Object value = arguments.get(name);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("'" + name + "' must be an integer");
        }
        long longValue = number.longValue();
        if (number.doubleValue() != longValue || longValue < minimum || longValue > maximum) {
            throw new IllegalArgumentException("'" + name + "' must be between " + minimum + " and " + maximum);
        }
        return (int) longValue;
    }

    public static List<String> optionalStringList(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> items)) {
            throw new IllegalArgumentException("'" + name + "' must be an array of strings");
        }
        List<String> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof String string) || string.isBlank()) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be a nonblank string");
            }
            result.add(string);
        }
        return result;
    }

    public static Map<String, Object> requiredObject(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (!(value instanceof Map<?, ?> source)) {
            throw new IllegalArgumentException("'" + name + "' must be an object");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, item) -> {
            if (!(key instanceof String stringKey)) {
                throw new IllegalArgumentException("'" + name + "' has an invalid property name");
            }
            result.put(stringKey, item);
        });
        return result;
    }

    public static List<Integer> requiredIntList(Map<String, Object> arguments, String name, int minimum, int maximum,
        int minItems, int maxItems) {
        Object value = arguments.get(name);
        if (!(value instanceof List<?> items) || items.size() < minItems || items.size() > maxItems) {
            throw new IllegalArgumentException("'" + name + "' must contain between " + minItems + " and " + maxItems + " integers");
        }
        List<Integer> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof Number number)) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be an integer");
            }
            long integer = number.longValue();
            if (number.doubleValue() != integer || integer < minimum || integer > maximum) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be between " + minimum + " and " + maximum);
            }
            result.add((int) integer);
        }
        return result;
    }

    public static List<Map<String, Object>> requiredObjectList(Map<String, Object> arguments, String name,
        int minItems, int maxItems) {
        Object value = arguments.get(name);
        if (!(value instanceof List<?> items) || items.size() < minItems || items.size() > maxItems) {
            throw new IllegalArgumentException("'" + name + "' must contain between " + minItems + " and " + maxItems + " objects");
        }
        List<Map<String, Object>> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> source)) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be an object");
            }
            Map<String, Object> object = new LinkedHashMap<>();
            source.forEach((key, field) -> {
                if (!(key instanceof String stringKey)) {
                    throw new IllegalArgumentException("'" + name + "' has an invalid property name");
                }
                object.put(stringKey, field);
            });
            result.add(object);
        }
        return result;
    }

    public static Map<String, Object> arguments(Map<String, Object> arguments) {
        return arguments == null ? Map.of() : arguments;
    }

    public static McpSchema.CallToolResult jsonResult(Object value) {
        return jsonResult(value, false);
    }

    public static McpSchema.CallToolResult jsonResult(Object value, boolean isError) {
        try {
            String text = McpJson.mapper().writeValueAsString(value);
            return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(text)))
                .isError(isError)
                .build();
        } catch (IOException e) {
            return errorResult("Unable to serialize tool result: " + e.getMessage());
        }
    }

    public static McpSchema.CallToolResult textResult(String text) {
        return McpSchema.CallToolResult.builder()
            .content(List.of(new McpSchema.TextContent(text)))
            .build();
    }

    public static McpSchema.CallToolResult errorResult(String message) {
        String detail = message == null || message.isBlank() ? "The operation failed." : message;
        return McpSchema.CallToolResult.builder()
            .content(List.of(new McpSchema.TextContent(detail)))
            .isError(true)
            .build();
    }

    public static McpSchema.CallToolResult invoke(Callable<McpSchema.CallToolResult> operation) {
        try {
            return operation.call();
        } catch (IllegalArgumentException | java.util.NoSuchElementException | IOException e) {
            return errorResult(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("MCP tool operation failed", e);
            return errorResult("The operation failed; see the application error log for details.");
        }
    }
}
