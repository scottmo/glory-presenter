package com.scottmo.mcp;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.core.songs.operations.SongOperations;

import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.spec.McpSchema;

/** Song library tools shared by STDIO and Streamable HTTP servers. */
public final class SongToolModule {
    private static final int MAX_ID = Integer.MAX_VALUE;

    private SongToolModule() {}

    public static <S extends SyncSpecification<S>> void register(S server, SongOperations operations) {
        Map<String, Object> stringArray = ToolSupport.arraySchema(ToolSupport.stringSchema(null), null);
        Map<String, Object> songSchema = songSchema();

        add(server, tool("songs_list", "List songs or search configured song descriptors. Each entry includes a stable id.",
            ToolSupport.objectSchema(Map.of(
                "query", ToolSupport.stringSchema("Case-insensitive descriptor search text."),
                "locales", stringArray
            )), true, false, true), args -> ToolSupport.jsonResult(operations.list(
                ToolSupport.optionalString(args, "query"), ToolSupport.optionalStringList(args, "locales"))));

        add(server, tool("song_get", "Read a song by stable id.", ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Stable song id.", 1, MAX_ID)
        ), "id"), true, false, true), args -> ToolSupport.jsonResult(
            operations.get(ToolSupport.requiredInt(args, "id", 1, MAX_ID))));

        add(server, tool("song_create", "Create a song with localized titles and verses.", ToolSupport.objectSchema(Map.of(
            "song", songSchema
        ), "song"), false, false, false), args -> {
            Song song = readSong(ToolSupport.requiredObject(args, "song"));
            int id = operations.create(song);
            return ToolSupport.jsonResult(Map.of("id", id, "song", operations.get(id)));
        });

        add(server, tool("song_update", "Replace a song's editable data by stable id.", ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Stable song id.", 1, MAX_ID),
            "song", songSchema
        ), "id", "song"), false, true, true), args -> {
            int id = ToolSupport.requiredInt(args, "id", 1, MAX_ID);
            int savedId = operations.update(id, readSong(ToolSupport.requiredObject(args, "song")));
            return ToolSupport.jsonResult(Map.of("id", savedId, "song", operations.get(savedId)));
        });

        add(server, tool("song_duplicate", "Duplicate a song into a new library entry.", ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Stable song id to duplicate.", 1, MAX_ID)
        ), "id"), false, false, false), args -> {
            int id = operations.duplicate(ToolSupport.requiredInt(args, "id", 1, MAX_ID));
            return ToolSupport.jsonResult(Map.of("id", id, "song", operations.get(id)));
        });

        Map<String, Object> idsSchema = ToolSupport.arraySchema(
            ToolSupport.integerSchema(null, 1, MAX_ID), "Song ids to delete; 1 to 100 unique ids.");
        idsSchema.put("minItems", 1);
        idsSchema.put("maxItems", 100);
        idsSchema.put("uniqueItems", true);
        add(server, tool("songs_delete", "Delete one or more songs by stable id.", ToolSupport.objectSchema(Map.of(
            "ids", idsSchema
        ), "ids"), false, true, false), args -> {
            List<Integer> ids = ToolSupport.requiredIntList(args, "ids", 1, MAX_ID, 1, 100);
            operations.delete(ids);
            return ToolSupport.jsonResult(Map.of("deletedIds", ids));
        });

        Map<String, Object> importSchema = new LinkedHashMap<>(ToolSupport.objectSchema(Map.of(
            "xml", ToolSupport.stringSchema("OpenLyrics XML document."),
            "path", ToolSupport.stringSchema("Path to a local OpenLyrics XML file.")), "xml"));
        importSchema.remove("required");
        importSchema.put("oneOf", List.of(Map.of("required", List.of("xml")), Map.of("required", List.of("path"))));
        add(server, tool("song_import_openlyrics", "Import one OpenLyrics song from XML text or a local file.", importSchema,
            false, false, false), args -> {
                String xml = ToolSupport.optionalString(args, "xml");
                String path = ToolSupport.optionalString(args, "path");
                if ((xml == null) == (path == null)) {
                    throw new IllegalArgumentException("Provide exactly one of 'xml' or 'path'");
                }
                int id = path == null ? operations.importOpenLyrics(xml) : operations.importOpenLyrics(Path.of(path));
                return ToolSupport.jsonResult(Map.of("id", id, "song", operations.get(id)));
            });

        Map<String, Object> exportSchema = ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Stable song id.", 1, MAX_ID),
            "path", ToolSupport.stringSchema("Optional destination path; when omitted, return content only.")
        ), "id");
        add(server, tool("song_export_openlyrics", "Export one song as OpenLyrics XML; optionally write it to a file.",
            exportSchema, false, true, true), args -> ToolSupport.jsonResult(operations.exportOpenLyrics(
                ToolSupport.requiredInt(args, "id", 1, MAX_ID), optionalPath(args, "path"))));
        add(server, tool("song_export_yaml", "Export one song as YAML; optionally write it to a file.",
            exportSchema, false, true, true), args -> ToolSupport.jsonResult(operations.exportYaml(
                ToolSupport.requiredInt(args, "id", 1, MAX_ID), optionalPath(args, "path"))));

        add(server, tool("song_generate_pptx", "Generate song slides from a PowerPoint template.", ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Stable song id.", 1, MAX_ID),
            "lines_per_slide", ToolSupport.integerSchema("Lyrics lines per slide, from 1 through 10.", 1, 10),
            "template", ToolSupport.stringSchema("Template filename relative to data/templates, or an absolute path."),
            "output_path", ToolSupport.stringSchema("Optional destination .pptx path.")
        ), "id", "lines_per_slide"), false, true, false), args -> ToolSupport.jsonResult(Map.of("path",
            operations.generatePowerpoint(
                ToolSupport.requiredInt(args, "id", 1, MAX_ID),
                ToolSupport.requiredInt(args, "lines_per_slide", 1, 10),
                ToolSupport.optionalString(args, "template"),
                optionalPath(args, "output_path")))));

        Map<String, Object> verseSchema = ToolSupport.objectSchema(Map.of(
            "name", ToolSupport.stringSchema("Verse/chorus identifier such as v1 or c1."),
            "text", ToolSupport.stringSchema(null),
            "locale", ToolSupport.stringSchema(null)
        ), "name");
        Map<String, Object> verseArraySchema = ToolSupport.arraySchema(verseSchema, "Unsaved or stored song verses.");
        verseArraySchema.put("maxItems", 500);
        add(server, tool("song_generate_verse_order", "Generate an order with each sorted verse followed by every sorted chorus.",
            ToolSupport.objectSchema(Map.of("verses", verseArraySchema), "verses"), true, false, true), args -> {
                List<SongVerse> verses = ToolSupport.requiredObjectList(args, "verses", 0, 500).stream()
                    .map(item -> McpJson.mapper().convertValue(item, SongVerse.class)).toList();
                return ToolSupport.jsonResult(Map.of("verseOrder", operations.generateVerseOrder(verses)));
            });
    }

    private static void add(SyncSpecification<?> server, McpSchema.Tool tool, ToolHandler handler) {
        server.toolCall(tool, (exchange, request) -> ToolSupport.invoke(
            () -> handler.call(ToolSupport.arguments(request.arguments()))));
    }

    private static McpSchema.Tool tool(String name, String description, Map<String, Object> schema,
        boolean readOnly, boolean destructive, boolean idempotent) {
        return ToolSupport.tool(name, description, schema,
            ToolSupport.annotations(name, readOnly, destructive, idempotent));
    }

    private static Map<String, Object> songSchema() {
        Map<String, Object> titleSchema = ToolSupport.objectSchema(Map.of(
            "text", ToolSupport.stringSchema(null),
            "locale", ToolSupport.stringSchema("Locale code, for example en_us.")
        ), "text", "locale");
        Map<String, Object> verseSchema = ToolSupport.objectSchema(Map.of(
            "name", ToolSupport.stringSchema("Verse/chorus identifier such as v1 or c1."),
            "text", ToolSupport.stringSchema(null),
            "locale", ToolSupport.stringSchema("Locale code, for example en_us.")
        ), "name", "text", "locale");
        return ToolSupport.objectSchema(Map.of(
            "id", ToolSupport.integerSchema("Ignored for create/update; the outer id determines the saved id.", 1, MAX_ID),
            "titles", ToolSupport.arraySchema(titleSchema, "Localized song titles."),
            "authors", ToolSupport.arraySchema(ToolSupport.stringSchema(null), null),
            "publisher", ToolSupport.stringSchema(null),
            "copyright", ToolSupport.stringSchema(null),
            "songBook", ToolSupport.stringSchema(null),
            "entry", ToolSupport.stringSchema(null),
            "comments", ToolSupport.stringSchema(null),
            "verseOrder", ToolSupport.arraySchema(ToolSupport.stringSchema(null), null),
            "verses", ToolSupport.arraySchema(verseSchema, "Localized lyrics, grouped by verse/chorus name.")
        ), "titles", "verses");
    }

    private static Song readSong(Map<String, Object> source) {
        return McpJson.mapper().convertValue(source, Song.class);
    }

    private static Path optionalPath(Map<String, Object> args, String name) {
        String value = ToolSupport.optionalString(args, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Path.of(value);
    }

    @FunctionalInterface
    private interface ToolHandler {
        McpSchema.CallToolResult call(Map<String, Object> arguments) throws Exception;
    }
}
