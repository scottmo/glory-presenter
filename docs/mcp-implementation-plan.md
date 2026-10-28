# MCP Implementation Plan

The MCP integration uses the official MCP Java SDK 2.1.0 with its Jackson 2 mapper and MCP protocol revision
2025-11-25. STDIO is the headless launch mode; the desktop configuration tab controls a loopback-only Streamable
HTTP endpoint. Application tool registrations belong in the shared `McpToolRegistry` so both transports expose the
same operations. Calls that touch existing stores or temporary PowerPoint files must use the application execution
lock introduced with those tools.

## Parity checklist

- [x] Phase 1: lifecycle, STDIO dispatch before Swing initialization, protocol-safe stdout, loopback HTTP start/stop,
  status tool, protocol handshake, Host/Origin validation, initial usage docs.
- [x] Phase 2: song list/search, read, create, update, delete, duplicate, verse-order generation, OpenLyrics import,
  XML/YAML export, and song PowerPoint generation.
- [x] Phase 3: Bible versions/books/verse queries, OSIS import, and Bible PowerPoint generation.
- [x] Phase 4: program PowerPoint generation, template list/read operations, and YAML map-template editing while
  preserving the existing default list-of-maps format.
- [x] Phase 5: formatter operations.
- [x] Phase 6: application configuration read/save/reload and theme toggle.
- [x] Phase 7: error-log read/clear/export.

The seven phases cover the application's domain and configuration operations. UI mechanics such as cursor position,
focus, selection, and undo history remain local interactions rather than separate MCP operations.

## Running

```powershell
java -jar target/glory-presenter-6.5.0-jar-with-dependencies.jar --mcp-server
java -Dglory.mcp.port=8766 -jar target/glory-presenter-6.5.0-jar-with-dependencies.jar
```

The second command changes the desktop application's local HTTP MCP port. The default is 8765. Keep the endpoint
bound to loopback and connect local clients only.
