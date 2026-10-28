package com.scottmo.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;

/** Builds the SDK's MCP mapper from the application's existing Jackson 2 stack. */
final class McpJson {
    private static final McpJsonMapper MAPPER = new JacksonMcpJsonMapper(new ObjectMapper());

    private McpJson() {}

    static McpJsonMapper mapper() {
        return MAPPER;
    }
}
