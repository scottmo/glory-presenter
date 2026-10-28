package com.scottmo.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scottmo.core.operations.FormatterOperations;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema;

/** MCP tools for batch PowerPoint formatting and newline normalization. */
public final class FormatterToolModule {
    private FormatterToolModule() {}

    public static <S extends SyncSpecification<S>> S register(S server, FormatterOperations operations) {
        server.toolCall(ToolSupport.tool("pptx_format_text",
                "Format matching text runs in one or more PowerPoint files. Slide indexes are zero-based; the end is exclusive, and endSlide 0 means all slides.",
                ToolSupport.objectSchema(Map.of(
                    "paths", ToolSupport.arraySchema(ToolSupport.stringSchema("A .pptx file path or directory; directories include immediate .pptx files."), "One or more file or directory paths."),
                    "startSlide", ToolSupport.integerSchema("Inclusive zero-based start slide; defaults to 0.", 0, Integer.MAX_VALUE),
                    "endSlide", ToolSupport.integerSchema("Exclusive zero-based end slide; 0 means all slides and defaults to 0.", 0, Integer.MAX_VALUE),
                    "matcher", ToolSupport.stringSchema("Configured text pattern preset name or regular expression."),
                    "format", ToolSupport.stringSchema("Configured text format preset name or YAML/JSON TextFormat object."),
                    "overwrite", booleanSchema("Overwrite source files; defaults to false and writes .mod.pptx files.")
                ), "paths", "matcher", "format"),
                ToolSupport.annotations("Format PowerPoint text", false, true, false)),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                FormatterOperations.BatchResult result = operations.formatText(
                    requiredStringList(args, "paths"),
                    optionalInteger(args, "startSlide"),
                    optionalInteger(args, "endSlide"),
                    ToolSupport.requiredString(args, "matcher"),
                    ToolSupport.requiredString(args, "format"),
                    optionalBoolean(args, "overwrite")
                );
                return ToolSupport.jsonResult(result, !result.failures().isEmpty());
            }));

        server.toolCall(ToolSupport.tool("pptx_normalize_newlines",
                "Split newline characters in text shapes into separate PowerPoint paragraphs for one or more files.",
                ToolSupport.objectSchema(Map.of(
                    "paths", ToolSupport.arraySchema(ToolSupport.stringSchema("A .pptx file path or directory; directories include immediate .pptx files."), "One or more file or directory paths."),
                    "overwrite", booleanSchema("Overwrite source files; defaults to false and writes .mod.pptx files.")
                ), "paths"),
                ToolSupport.annotations("Normalize PowerPoint newlines", false, true, false)),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                FormatterOperations.BatchResult result = operations.normalizeNewlines(
                    requiredStringList(args, "paths"), optionalBoolean(args, "overwrite"));
                return ToolSupport.jsonResult(result, !result.failures().isEmpty());
            }));
        return server;
    }

    private static Map<String, Object> booleanSchema(String description) {
        return Map.of("type", "boolean", "description", description);
    }

    private static List<String> requiredStringList(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (!(value instanceof List<?> items) || items.isEmpty()) {
            throw new IllegalArgumentException("'" + name + "' must be a nonempty array of file or directory paths");
        }
        List<String> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof String path) || path.isBlank()) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be a nonblank string");
            }
            result.add(path);
        }
        return result;
    }

    private static int optionalInteger(Map<String, Object> arguments, String name) {
        if (!arguments.containsKey(name) || arguments.get(name) == null) {
            return 0;
        }
        Object value = arguments.get(name);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("'" + name + "' must be an integer");
        }
        long integer = number.longValue();
        if (number.doubleValue() != integer || integer < 0 || integer > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("'" + name + "' must be a nonnegative integer");
        }
        return (int) integer;
    }

    private static boolean optionalBoolean(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (value == null) {
            return false;
        }
        if (!(value instanceof Boolean bool)) {
            throw new IllegalArgumentException("'" + name + "' must be a boolean");
        }
        return bool;
    }
}
