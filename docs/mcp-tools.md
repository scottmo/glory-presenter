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

## Bible library

`bible_list_versions` returns imported Bible version IDs. `bible_list_books` returns the canonical English book IDs
supported by the application. `bible_get_book_names` accepts `{ "bookId": "john" }` and returns a map from version ID to
the stored display name for that book.

`bible_get_verses` accepts `{ "reference": "john 1:1-3;3:16" }` and optionally a `versions` string array. An explicit
version prefix in the reference, such as `niv,cuv - john 1:1-3`, selects versions directly. Without a prefix, the
tool uses `versions` when supplied or the configured `bibleVersionToLocale` keys otherwise. Do not supply both a
prefix and `versions`. References support chapter-only, verse, comma-separated verse lists/ranges, and semicolon
separated chapter ranges. Chapter and verse bounds are checked against the canonical book metadata. The result maps
each version to a list of `{ "bookIndex", "chapter", "index", "text" }` verse records.

`bible_import_osis` accepts `{ "filePaths": ["/path/to/bible.xml", ...] }`. Files are imported independently; the
result contains `imported` paths and a `failures` map from path to error message. If any file fails, the result still
contains successes and is marked `isError: true`. OSIS imports may replace existing verses for the same version.

`bible_generate_pptx` accepts `{ "reference": "niv - john 1:1-3" }`, with optional `template` and `outputPath`. The
configured Bible template is used when `template` is omitted. A relative template name is resolved beneath
`<dataDir>/templates`; an absolute local template path is also accepted. Without `outputPath`, the configured output
directory and reference determine the filename. Relative output paths resolve beneath the configured output
directory; supplied destinations can be overwritten.

```json
{"name":"bible_list_versions","arguments":{}}
```

```json
{"name":"bible_get_verses","arguments":{"reference":"john 1:1-3","versions":["niv"]}}
```

```json
{"name":"bible_import_osis","arguments":{"filePaths":["C:/bibles/niv.xml","C:/bibles/cuv.xml"]}}
```

```json
{"name":"bible_generate_pptx","arguments":{"reference":"niv - john 1:1-3","template":"bible.pptx"}}
```

## Program templates and generation

`program_list_templates` lists files recursively under `<dataDir>/templates` using paths relative to that directory.
`program_list_program_templates` returns only files whose basename matches `program-*.txt` (case-insensitive), also
including nested directories. `program_read_template` reads any listed relative template path;
`program_read_program_template` additionally requires a `program-*.txt` basename. Absolute paths and paths that
escape the templates directory are rejected.

`program_generate` accepts `{ "yaml": "..." }` and optional `outputPath`. The YAML is a list of slide-set objects
with `type` (`default`, `styles`, `bible`, or `song`), optional `template`, and `content`. A missing `type` defaults
to `default`; a missing template uses the matching configured default. Template names are relative to
`<dataDir>/templates`. Without `outputPath`, the merged deck is written to `<outputDir>/output.pptx`; relative output
paths resolve beneath `<outputDir>`, and the destination may be overwritten.

For `default` and `styles` slide sets, `content` is a YAML list of maps. The first map supplies metadata for the first
and last template slides; subsequent maps supply content slides. Preserve this list-of-maps format. When the first
and last slides have no placeholders, use an empty first map written as `- {}`. Bible content is a Bible reference;
song content is a YAML map with `songId` and `linesPerSlide`.

```json
{"name":"program_list_program_templates","arguments":{}}
```

```json
{"name":"program_read_template","arguments":{"path":"worship/program-sunday.txt"}}
```

```json
{"name":"program_generate","arguments":{"yaml":"- type: default\n  template: worship/default.pptx\n  content: |\n    - {}\n    - title-en: Welcome\n      subtitle-en: Sunday Service\n- type: bible\n  template: bible.pptx\n  content: john 1:1-3\n","outputPath":"sunday-service.pptx"}}
```

## PowerPoint formatter

`pptx_format_text` accepts `paths` (a nonempty array of `.pptx` paths and/or directories), optional `startSlide` and
`endSlide`, `matcher`, `format`, and optional `overwrite`. Directories include their immediate `.pptx` files. Slide
indexes are zero-based; `startSlide` is inclusive and defaults to 0, while `endSlide` is exclusive and defaults to 0.
An `endSlide` of 0 means all slides from `startSlide` onward. `matcher` is a name from the configured
`patternPresets` map or a Java regular expression. `format` is a configured `textFormatPresets` name or a YAML/JSON
`TextFormat` object with optional `fontFamily`, `fontSize`, `fontColor` (`"R, G, B"`), `bold`, `italic`,
`underlined`, `strikethrough`, and `smallCaps` fields.

`pptx_normalize_newlines` accepts the same `paths` array and optional `overwrite`; it converts newlines inside text
shapes into separate paragraphs. With `overwrite: false` (the default), both tools write a sibling file with
`.mod.pptx` appended before the extension, preserving the original extension's casing (for example,
`slides.PPTX` becomes `slides.mod.PPTX`). With `overwrite: true`, the input is replaced.

Both tools return `successes` and `failures` arrays. Each success includes its input and resolved output paths; each
failure includes the input path, intended output path, and error message. The result is marked `isError: true` if any
file fails, while preserving other files' results. Invalid ranges, matcher patterns, or format values are reported as
tool errors before batch processing begins.

```json
{"name":"pptx_format_text","arguments":{"paths":["C:/slides/service.PPTX"],"startSlide":1,"endSlide":0,"matcher":"title","format":"worshipTitle"}}
```

```json
{"name":"pptx_format_text","arguments":{"paths":["C:/slides/service.pptx"],"matcher":"\\bAmen\\b","format":"fontFamily: Arial\nfontSize: 28\nfontColor: 255, 255, 255\nbold: true"}}
```

```json
{"name":"pptx_normalize_newlines","arguments":{"paths":["C:/slides"],"overwrite":false}}
```

## Application configuration and theme

`config_get` returns a defensive snapshot of the complete application configuration. It includes the locale list,
data/output paths, default templates, Bible locale mapping, formatter presets, UI settings, and `darkMode`. The
runtime-only `templatePaths` list is derived from the templates directory and is not part of the JSON snapshot.

`config_save` accepts `{ "json": "<complete JSON configuration>" }`. Treat the result from `config_get` as the
starting document: edit the fields you intend to change, then submit the complete document. Saving a partial object
does not merge omitted values. Configuration values are validated before any file is replaced. A successful save
uses an atomic file replacement and activates the new settings; validation or filesystem errors leave the previous
configuration file and active settings in place. Relative `dataDir` and `outputDir` values are kept relative to the
application's working directory, so configurations remain portable. Changing `dataDir` switches cached song and
Bible services to the new database directory on their next operation; it does not move or copy databases from the
previous directory.

`config_reload` validates and activates the configuration already on disk. If the file is invalid, the currently
active settings remain in use. FlatLaf `uiScale` values may use a number (`1.5`), multiplier (`1.5x`), percentage
(`150%`), or DPI value (`144dpi`), following [FlatLaf's system property syntax](https://www.formdev.com/flatlaf/system-properties/).

`config_set_dark_mode` accepts `{ "darkMode": boolean }` and persists the theme preference. A running desktop
instance queues theme, font, scale, and window-size updates on Swing's event thread. A headless MCP process stores
the preference for the next desktop launch.

Example read-modify-save flow:

```json
{"name":"config_get","arguments":{}}
```

After editing the returned JSON document, submit the whole document as the `json` string:

```json
{"name":"config_save","arguments":{"json":"{\"uiScale\":\"1.5x\",\"appDimensions\":\"1440x900\",\"appFontSize\":14,\"outputDir\":\"./output\",\"dataDir\":\"./data\",\"uiFontFamily\":\"Segoe UI\",\"darkMode\":true,\"locales\":[\"en_us\"],\"bibleVersionToLocale\":{\"niv\":\"en_us\"},\"defaultTemplates\":{\"song\":\"song.pptx\",\"bible\":\"bible.pptx\",\"default\":\"default.pptx\"},\"patternPresets\":{\"title\":\"^.+$\"},\"textFormatPresets\":{}}"}}
```

```json
{"name":"config_set_dark_mode","arguments":{"darkMode":false}}
```
