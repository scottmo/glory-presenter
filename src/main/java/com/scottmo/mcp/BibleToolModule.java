package com.scottmo.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.scottmo.core.operations.BibleOperations;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema;

/** MCP tools for Bible catalog, lookup, OSIS import, and PowerPoint generation. */
public final class BibleToolModule {
    private BibleToolModule() {}

    public static <S extends SyncSpecification<S>> S register(S server, BibleOperations operations) {
        server.toolCall(tool("bible_list_versions", "List Bible versions currently imported into the application.",
                ToolSupport.objectSchema(Map.of())),
            (exchange, request) -> ToolSupport.invoke(() ->
                ToolSupport.jsonResult(operations.listVersions())));

        server.toolCall(tool("bible_list_books", "List the canonical book IDs supported by the application.",
                ToolSupport.objectSchema(Map.of())),
            (exchange, request) -> ToolSupport.invoke(() ->
                ToolSupport.jsonResult(operations.listBooks())));

        server.toolCall(tool("bible_get_book_names", "Get imported localized display names for one canonical Bible book ID.",
                ToolSupport.objectSchema(Map.of(
                    "bookId", ToolSupport.stringSchema("Canonical book ID, for example 'john'.")
                ), "bookId")),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                return ToolSupport.jsonResult(operations.getBookNames(
                    ToolSupport.requiredString(args, "bookId")));
            }));

        server.toolCall(tool("bible_get_verses", "Get verses for a Bible reference. If no version prefix is present, use configured versions or the optional versions list.",
                ToolSupport.objectSchema(Map.of(
                    "reference", ToolSupport.stringSchema("Reference such as 'john 1:1-3;3:16' or 'niv - john 1:1-3'."),
                    "versions", ToolSupport.arraySchema(ToolSupport.stringSchema("Imported version ID."), "Optional versions when the reference has no version prefix.")
                ), "reference")),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                return ToolSupport.jsonResult(operations.getVerses(
                    ToolSupport.requiredString(args, "reference"),
                    ToolSupport.optionalStringList(args, "versions")));
            }));

        server.toolCall(tool("bible_import_osis", "Import one or more OSIS XML Bible files. Returns successes and per-file failures.",
                ToolSupport.objectSchema(Map.of(
                    "filePaths", ToolSupport.arraySchema(ToolSupport.stringSchema("Path to an OSIS XML file."), "Files are imported independently so one failure does not discard other results.")
                ), "filePaths")),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                BibleOperations.ImportResult result = operations.importOsis(requiredStringList(args, "filePaths"));
                return ToolSupport.jsonResult(result, !result.failures().isEmpty());
            }));

        server.toolCall(tool("bible_generate_pptx", "Generate Bible slides using the configured or supplied template and output path.",
                ToolSupport.objectSchema(Map.of(
                    "reference", ToolSupport.stringSchema("Bible reference, optionally prefixed with version IDs and ' - '."),
                    "template", ToolSupport.stringSchema("Optional template filename or local path; defaults to the configured Bible template."),
                    "outputPath", ToolSupport.stringSchema("Optional output .pptx path; defaults to the configured output directory.")
                ), "reference")),
            (exchange, request) -> ToolSupport.invoke(() -> {
                Map<String, Object> args = ToolSupport.arguments(request.arguments());
                return ToolSupport.jsonResult(Map.of("outputPath", operations.generatePowerpoint(
                    ToolSupport.requiredString(args, "reference"),
                    ToolSupport.optionalString(args, "template"),
                    ToolSupport.optionalString(args, "outputPath"))));
            }));
        return server;
    }

    private static List<String> requiredStringList(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (!(value instanceof List<?> items) || items.isEmpty()) {
            throw new IllegalArgumentException("'" + name + "' must be a nonempty array of file paths");
        }
        List<String> paths = new ArrayList<>(items.size());
        for (Object item : items) {
            if (!(item instanceof String path) || path.isBlank()) {
                throw new IllegalArgumentException("Every value in '" + name + "' must be a nonblank string");
            }
            paths.add(path);
        }
        return paths;
    }

    private static McpSchema.Tool tool(String name, String description, Map<String, Object> schema) {
        boolean readOnly = name.startsWith("bible_list_") || name.startsWith("bible_get_");
        boolean destructive = name.equals("bible_import_osis") || name.equals("bible_generate_pptx");
        return ToolSupport.tool(name, description, schema,
            ToolSupport.annotations(name, readOnly, destructive, readOnly));
    }
}
