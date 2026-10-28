# Glory Presenter

## Introduction

Glory presenter is an app that's solely focused on the power point experience.
Modern options such as ProPresenter, OpenLP, Quelea, etc. are all very good options if a church has a dedicated
department or staffs to maintain and operate. Unfortunately, this is not always feasible for small churches and
churches that don't have the necessary equipments and talents. For example, in my church, preachers always bring
their own power point slides for the sermons. The slides are usually in pptx format with different styles and themes.
Modern worship software usually have the option to load external power point, but they are all pretty slow and
non-responsive, especially when there are videos or audios embedded in the slides. The staffs in my church are also
limited in numbers. I'm pretty much the only one handling the whole worship presentation. When I'm sick or out, it's
important to get someone that's untrained to help out right away. Using modern worship software becomes a
difficult option as they all require training to use, whereas most people have experience operating a power
point deck.

## Caveats

This application is developed in spare time and remains under active development. Error handling and supported
workflows continue to evolve.

## Technology

- Java Swing
- Java 17
- SQLite

## Features

- Song library: add, edit, duplicate, delete, search, import OpenLyrics, and export OpenLyrics or YAML.
- Bible library: import OSIS, inspect books and versions, and look up passages.
- Program templates: browse/read text templates and generate decks from YAML.
- PowerPoint creation from songs and Bible passages.
- PowerPoint text formatting and newline normalization.
- Application configuration, dark-mode toggle, and error-log viewing, clearing, and export.
- Song import format: OpenLyrics (<https://docs.openlyrics.org/>); Bible import format: OSIS (<https://crosswire.org/osis/>).

## MCP Server

The official MCP Java SDK is integrated using STDIO and Streamable HTTP transports. To run the headless STDIO
server, launch the packaged application with `--mcp-server` (for example, `java -jar target/app.jar --mcp-server`).
The server handles MCP over stdin/stdout and exits cleanly when the client closes stdin. Protocol messages are the
only content written to stdout; diagnostics are sent to stderr.

The desktop app can start or stop a loopback-only Streamable HTTP endpoint from the Configurations tab. Its default
address is `http://127.0.0.1:8765/mcp`; set the Java system property `-Dglory.mcp.port=PORT` to use another port.
The HTTP server binds to 127.0.0.1 and rejects untrusted Host and Origin headers. Do not expose this local endpoint
through a public or network-facing proxy.

The server exposes the song, Bible, program-template, PowerPoint-formatting, configuration/theme, and application-log
operations, plus `mcp_status`. UI-only cursor position, focus, selection, and undo history are not separate operations.
See [the MCP tool reference](docs/mcp-tools.md) for schemas, examples, file behavior, and errors. The server uses MCP
protocol version 2025-11-25 through the released Java SDK 2.1.0.

### Build and launch

Use JDK 17 and Maven. From the repository root, run:

```powershell
mvn -B test
mvn -B package
java -jar target/app.jar --mcp-server
```

Packaging creates the executable fat JAR at `target/app.jar`. The application
loads or creates `data/config.json` relative to its process working directory. The configured `dataDir` and
`outputDir`, and other relative paths, are also resolved from that working directory. Run the server from the
directory containing the intended `data/config.json` so it finds the intended config and stores.

A typical STDIO MCP client command/args entry is:

```json
{
  "command": "java",
  "args": [
    "-jar",
    "C:/apps/glory-presenter/target/app.jar",
    "--mcp-server"
  ]
}
```

Set the client's working directory to the directory containing the intended `data/config.json` when the client
supports that setting. Client support varies; otherwise use a platform-appropriate launcher script that changes to
that directory before starting Java. STDIO clients communicate over stdin/stdout, so do not print launcher messages
to stdout.

For desktop use, open the Configurations tab and click **Start MCP** to start, or **Stop MCP** to stop, the local
HTTP server. Its endpoint is `http://127.0.0.1:8765/mcp`; set `-Dglory.mcp.port=PORT` before launching Java to choose
another port. The server binds to loopback and rejects untrusted Host and Origin headers. Do not expose this endpoint
through a public or network-facing proxy.

## Sample App Config

### Directory

- glory-presenter
  - app.jar
  - config.json
  - output
  - data
    - song.db
    - bible.db
    - templates
      - template-bible.pptx
      - template-song.pptx

### config.json

```json
{
  "dataDir": "./data",
  "outputDir": "./output",
  "locales": [
    "zh_cn",
    "en_us"
  ],
  "bibleVersionToLocale": {
    "cuv": "zh_cn",
    "niv": "en_us"
  }
}
```

## PPTX Generation

Generation is  based off of an existing pptx template. Placeholder texts in the pptx template will be replaced with
the actual values fromm the local database. See samples folder for examples.

### Placeholders

- bible slides - e.g. genesis 1:2, assuming $version is asv
  - {verse.chapter} - 1
  - {verse.number} - 2
  - {verses} - 1:2
  - {verse.$version} - In the beginning God created the heavens and the earth.
  - {book.$version} - Genesis
- song slildes
  - {songbook}
  - {entry}
  - {copyright}
  - {publisher}
  - {verse.$locale}
  - {title.$locale}
