package com.scottmo.mcp;

import java.util.Map;

import com.scottmo.core.operations.ConfigOperations;

import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema;

/** Configuration tools shared by STDIO and Streamable HTTP transports. */
public final class ConfigToolModule {
    private static final int MAX_JSON_LENGTH = 1_000_000;

    private ConfigToolModule() {}

    public static <S extends SyncSpecification<S>> void register(S server, ConfigOperations operations) {
        add(server, "config_get", "Read a defensive snapshot of the full application configuration.",
            ToolSupport.objectSchema(Map.of()), true, false, true,
            args -> ToolSupport.jsonResult(operations.snapshot()));
        add(server, "config_save", "Validate and atomically save a complete application configuration JSON document.",
            ToolSupport.objectSchema(Map.of("json", ToolSupport.stringSchema("Full configuration JSON.")), "json"),
            false, true, true, args -> {
                String json = ToolSupport.requiredString(args, "json");
                if (json.length() > MAX_JSON_LENGTH) {
                    throw new IllegalArgumentException("Configuration JSON exceeds 1 MB");
                }
                return ToolSupport.jsonResult(operations.saveJson(json));
            });
        add(server, "config_reload", "Reload and validate the current configuration file.",
            ToolSupport.objectSchema(Map.of()), false, false, true,
            args -> ToolSupport.jsonResult(operations.reload()));
        add(server, "config_set_dark_mode", "Persist the theme preference and apply it to a running desktop UI.",
            ToolSupport.objectSchema(Map.of("darkMode", Map.of("type", "boolean")), "darkMode"),
            false, false, true, args -> {
                Object value = args.get("darkMode");
                if (!(value instanceof Boolean darkMode)) {
                    throw new IllegalArgumentException("'darkMode' must be a boolean");
                }
                return ToolSupport.jsonResult(operations.setDarkMode(darkMode));
            });
    }

    private static void add(SyncSpecification<?> server, String name, String description,
        Map<String, Object> schema, boolean readOnly, boolean destructive, boolean idempotent, Handler handler) {
        server.toolCall(ToolSupport.tool(name, description, schema,
            ToolSupport.annotations(name, readOnly, destructive, idempotent)),
            (exchange, request) -> ToolSupport.invoke(() -> handler.call(ToolSupport.arguments(request.arguments()))));
    }

    @FunctionalInterface
    private interface Handler {
        McpSchema.CallToolResult call(Map<String, Object> args) throws Exception;
    }
}
