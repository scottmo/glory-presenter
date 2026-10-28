# MCP Tool Reference

All tools are available through both the headless STDIO server and the desktop application's loopback-only Streamable
HTTP server. Tool inputs are JSON objects. IDs are stable integer database IDs; tools never identify a song by title.
Expected input, missing-file, and missing-record errors return an MCP tool result with `isError: true`.

## Song library

`songs_list` accepts optional `query` and `locales` fields. Search is case-insensitive over song descriptors; if
`locales` is omitted, the configured locales are used. The result is a list of `{ "id", "descriptor" }` entries.

`song_get` accepts `{ "id": integer }` and returns a song object with localized `titles`, `verses`, `authors`,
`verseOrder`, publisher/copyright/songbook metadata, and comments.

`song_create` accepts `{ "song": Song }`; `song_update` accepts `{ "id": integer, "song": Song }`. A song has
`titles` (array of `{ "text", "locale" }`), `verses` (array of `{ "name", "text", "locale" }`), and optional
`authors`, `publisher`, `copyright`, `songBook`, `entry`, `comments`, and `verseOrder`. Title locales and
verse-name/locale pairs must be unique. `verseOrder` may contain only names present in `verses`. The outer `id` on
`song_update` determines which record is replaced. `song_duplicate` accepts `{ "id": integer }` and creates a new
song record. `songs_delete` accepts `{ "ids": [integer, ...] }` with 1–100 unique IDs.

`song_import_openlyrics` accepts exactly one of `{ "xml": "..." }` or `{ "path": "..." }`. File imports are limited
to 5 MiB. `song_export_openlyrics` and `song_export_yaml` accept `{ "id": integer }` and return serialized
`content`; an optional `path` writes the content to that destination, creating parent directories. Relative export
paths are resolved beneath the configured output directory. Exports that name a path overwrite that file.

`song_generate_verse_order` accepts `{ "verses": [{ "name": "v1" }, ...] }` and returns a sorted sequence of
verse names, with all sorted chorus names following each verse.

`song_generate_pptx` accepts `{ "id": integer, "lines_per_slide": integer }` and optional `template` and
`output_path`. `lines_per_slide` must be between 1 and 10. A relative template name resolves from
`<dataDir>/templates`; an omitted template uses `defaultTemplates.song` from the application configuration. If no
`output_path` is supplied, the configured output directory and song title determine the filename. Relative output
paths resolve beneath the configured output directory; supplied destinations can be overwritten.

## Examples

```json
{"name":"songs_list","arguments":{"query":"amazing grace"}}
```

```json
{"name":"song_create","arguments":{"song":{"titles":[{"text":"Amazing Grace","locale":"en_us"}],"authors":["John Newton"],"verses":[{"name":"v1","text":"Amazing grace, how sweet the sound","locale":"en_us"}],"verseOrder":["v1"]}}}
```

IDs returned from `song_create` can be passed to `song_get`, `song_update`, `song_duplicate`, exports, slide generation,
and `songs_delete`.
