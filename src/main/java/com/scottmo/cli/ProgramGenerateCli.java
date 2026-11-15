package com.scottmo.cli;

import com.scottmo.core.ServiceProvider;
import com.scottmo.core.operations.ProgramOperations;
import com.scottmo.core.ppt.api.PowerpointService;
import com.scottmo.shared.JsonUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Command-line entry point for generating a program deck. */
public final class ProgramGenerateCli {
    private static final String USAGE = """
        Usage: java -jar target/app.jar program generate --config <program.yaml> [--output <deck.pptx>] [--json]

        Options:
          --config <file>  Program YAML configuration to generate (required; not data/config.json)
          --output <file>  Output deck path. Relative paths are resolved under the configured outputDir.
          --json           Print the result as JSON: {\"outputPath\":\"...\"}
          -h, --help       Show this help
        """;
    private static final String ROOT_USAGE = """
        Usage: java -jar target/app.jar <command>

        Commands:
          program generate  Generate a PowerPoint program deck from YAML
          --mcp-server     Start the headless MCP server
          --help           Show this help
        """;

    private ProgramGenerateCli() {}

    /**
     * Runs the command and returns a process exit code. Application and library stdout is redirected
     * to stderr while services initialize and generate, leaving stdout for the command result only.
     */
    public static int run(String[] args, PrintStream stdout, PrintStream stderr) {
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            stdout.print(ROOT_USAGE);
            return 0;
        }
        if (args.length == 2 && "program".equals(args[0])
            && ("--help".equals(args[1]) || "-h".equals(args[1]))) {
            stdout.print(USAGE);
            return 0;
        }
        Options options;
        try {
            options = parse(args);
        } catch (IllegalArgumentException e) {
            stderr.println("program generate: " + e.getMessage());
            stderr.print(USAGE);
            return 2;
        }
        if (options.help()) {
            stdout.print(USAGE);
            return 0;
        }

        PrintStream previousOut = System.out;
        Path generated;
        try {
            System.setOut(stderr);
            String yaml = Files.readString(options.config(), StandardCharsets.UTF_8);
            generated = ProgramOperations.configured(ServiceProvider.get(PowerpointService.class).get())
                .generate(yaml, options.output());
        } catch (Exception e) {
            stderr.println("program generate: " + message(e));
            return 1;
        } finally {
            System.setOut(previousOut);
        }

        try {
            if (options.json()) {
                Map<String, String> result = new LinkedHashMap<>();
                result.put("outputPath", generated.toString());
                stdout.println(JsonUtil.mapper().writeValueAsString(result));
            } else {
                stdout.println(generated);
            }
            return 0;
        } catch (IOException e) {
            stderr.println("program generate: unable to write result: " + message(e));
            return 1;
        }
    }

    private static Options parse(String[] args) {
        if (args.length < 2 || !"program".equals(args[0]) || !"generate".equals(args[1])) {
            throw new IllegalArgumentException("expected `program generate`");
        }
        Path config = null;
        String output = null;
        boolean json = false;
        boolean help = false;
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> help = true;
                case "--config" -> {
                    if (config != null) throw new IllegalArgumentException("--config may be specified only once");
                    if (++i >= args.length || args[i].isBlank() || args[i].startsWith("-")) {
                        throw new IllegalArgumentException("--config requires a program YAML file");
                    }
                    config = Path.of(args[i]);
                }
                case "--output" -> {
                    if (output != null) throw new IllegalArgumentException("--output may be specified only once");
                    if (++i >= args.length || args[i].isBlank() || args[i].startsWith("-")) {
                        throw new IllegalArgumentException("--output requires a file path");
                    }
                    output = args[i];
                }
                case "--json" -> {
                    if (json) throw new IllegalArgumentException("--json may be specified only once");
                    json = true;
                }
                default -> throw new IllegalArgumentException("unknown option: " + args[i]);
            }
        }
        if (!help && config == null) throw new IllegalArgumentException("--config is required");
        return new Options(config, output, json, help);
    }

    private static String message(Exception error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    private record Options(Path config, String output, boolean json, boolean help) {}
}
