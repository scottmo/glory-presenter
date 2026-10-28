package com.scottmo.mcp;

import com.scottmo.core.operations.LogOperations;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

/** MCP tools for the same in-memory log shown in the Error Log tab. */
public final class LogToolModule {
    private LogToolModule() {}

    public static <S extends SyncSpecification<S>> S register(S server, LogOperations operations) {
        Map<String, Object> readProperties = new LinkedHashMap<>();
        readProperties.put("limit", ToolSupport.integerSchema("Optional maximum number of most recent log entries.", 1, 100000));
        server.toolCall(tool("log_read", "Read the current in-memory application log.",
                ToolSupport.objectSchema(readProperties), true, false, true),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> arguments = ToolSupport.arguments(request.arguments());
                Integer limit = optionalLimit(arguments);
                var logs = operations.read(limit);
                return ToolSupport.jsonResult(Map.of("count", logs.size(), "logs", logs));
            }));

        server.toolCall(tool("log_clear", "Clear the in-memory application log and update open Error Log views.",
                ToolSupport.objectSchema(Map.of()), false, true, false),
            (exchange, request) -> ToolSupport.invoke(() ->
                ToolSupport.jsonResult(Map.of("clearedEntries", operations.clear()))));

        server.toolCall(tool("log_export", "Export the in-memory application log as UTF-8 to error.log or a supplied path.",
                ToolSupport.objectSchema(Map.of(
                    "path", ToolSupport.stringSchema("Optional export path. Defaults to error.log in the current working directory.")
                )), false, true, false),
            (exchange, request) -> ToolSupport.invoke(() -> {
                String path = ToolSupport.optionalString(ToolSupport.arguments(request.arguments()), "path");
                return ToolSupport.jsonResult(Map.of("exportedPath", operations.export(path).toString()));
            }));
        return server;
    }

    private static Integer optionalLimit(Map<String, Object> arguments) {
        Object value = arguments.get("limit");
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("'limit' must be an integer");
        }
        long integerValue = number.longValue();
        if (number.doubleValue() != integerValue || integerValue < 1 || integerValue > 100000) {
            throw new IllegalArgumentException("'limit' must be between 1 and 100000");
        }
        return (int) integerValue;
    }

    private static io.modelcontextprotocol.spec.McpSchema.Tool tool(String name, String description,
            Map<String, Object> schema, boolean readOnly, boolean destructive, boolean idempotent) {
        return ToolSupport.tool(name, description, schema,
            ToolSupport.annotations(name, readOnly, destructive, idempotent));
    }
}
