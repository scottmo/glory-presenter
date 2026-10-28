package com.scottmo.mcp;

import java.util.Map;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema;

/** Central registration point shared by STDIO and Streamable HTTP servers. */
final class McpToolRegistry {
    private McpToolRegistry() {}

    static <S extends SyncSpecification<S>> S register(S server) {
        McpSchema.Tool statusTool = McpSchema.Tool.builder()
            .name("mcp_status")
            .description("Return the Glory Presenter MCP server name and version.")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(),
                "additionalProperties", false
            ))
            .build();

        server.serverInfo("glory-presenter", "6.5.0");
        server.capabilities(McpSchema.ServerCapabilities.builder().tools(true).build());
        server.toolCall(statusTool, (exchange, request) -> McpSchema.CallToolResult.builder()
                .content(java.util.List.of(new McpSchema.TextContent("Glory Presenter MCP server 6.5.0")))
                .build());
        return server;
    }

    static McpServer.SingleSessionSyncSpecification createStdio(
        io.modelcontextprotocol.spec.McpServerTransportProvider transport
    ) {
        return register(McpServer.sync(transport));
    }

    static McpServer.StreamableSyncSpecification createHttp(
        io.modelcontextprotocol.spec.McpStreamableServerTransportProvider transport
    ) {
        return register(McpServer.sync(transport));
    }
}
