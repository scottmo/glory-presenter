package com.scottmo.mcp;

import com.scottmo.core.operations.ProgramOperations;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import java.util.Map;

/** MCP tools for listing templates, reading program YAML, and generating program decks. */
public final class ProgramToolModule {
    private ProgramToolModule() {}

    public static <S extends SyncSpecification<S>> S register(S server, ProgramOperations operations) {
        server.toolCall(tool("program_list_templates", "List all files recursively under the templates directory.",
                ToolSupport.objectSchema(Map.of()), true, false, true),
            (exchange, request) -> ToolSupport.invoke(() ->
                ToolSupport.jsonResult(operations.listTemplates())));

        server.toolCall(tool("program_list_program_templates", "List program-*.txt files recursively under templates.",
                ToolSupport.objectSchema(Map.of()), true, false, true),
            (exchange, request) -> ToolSupport.invoke(() ->
                ToolSupport.jsonResult(operations.listProgramTemplates())));

        server.toolCall(tool("program_read_template", "Read a template file by its relative path under templates.",
                ToolSupport.objectSchema(Map.of("path", ToolSupport.stringSchema("Relative path returned by a template listing.")), "path"), true, false, true),
            (exchange, request) -> ToolSupport.invoke(() -> ToolSupport.textResult(
                operations.readTemplate(ToolSupport.requiredString(request.arguments(), "path")))));

        server.toolCall(tool("program_read_program_template", "Read a program-*.txt YAML template by its relative path.",
                ToolSupport.objectSchema(Map.of("path", ToolSupport.stringSchema("Relative path returned by the program template listing.")), "path"), true, false, true),
            (exchange, request) -> ToolSupport.invoke(() -> ToolSupport.textResult(
                operations.readProgramTemplate(ToolSupport.requiredString(request.arguments(), "path")))));

        server.toolCall(tool("program_generate", "Generate and merge a PowerPoint from the Program tab YAML configuration.",
                ToolSupport.objectSchema(Map.of(
                    "yaml", ToolSupport.stringSchema("Program configuration using the existing default, song, and Bible format."),
                    "outputPath", ToolSupport.stringSchema("Optional output file path. Defaults to output/output.pptx.")
                ), "yaml"), false, true, false),
            (exchange, request) -> ToolSupport.invoke(() -> {
                String yaml = ToolSupport.requiredString(request.arguments(), "yaml");
                String outputPath = ToolSupport.optionalString(request.arguments(), "outputPath");
                return ToolSupport.jsonResult(Map.of("outputPath", operations.generate(yaml, outputPath).toString()));
            }));
        return server;
    }

    private static io.modelcontextprotocol.spec.McpSchema.Tool tool(String name, String description,
            Map<String, Object> schema, boolean readOnly, boolean destructive, boolean idempotent) {
        return ToolSupport.tool(name, description, schema,
            ToolSupport.annotations(name, readOnly, destructive, idempotent));
    }
}
