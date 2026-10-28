package com.scottmo.mcp;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.DefaultServerTransportSecurityValidator;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;

/** Lifecycle owner for the optional loopback-only HTTP endpoint. */
public final class McpServerManager {
    public static final int DEFAULT_PORT = 8765;
    private static final McpServerManager INSTANCE = new McpServerManager();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop, "mcp-server-shutdown"));
    }

    public static McpServerManager get() {
        return INSTANCE;
    }

    private final AtomicReference<RunningServer> running = new AtomicReference<>();

    private McpServerManager() {}

    public synchronized String start() throws Exception {
        if (running.get() != null) {
            return endpoint();
        }

        int port = configuredPort();
        String hostPort = "127.0.0.1:" + port;
        String localhostPort = "localhost:" + port;
        var validator = DefaultServerTransportSecurityValidator.builder()
            .allowedHosts(List.of(hostPort, localhostPort))
            .allowedOrigins(List.of("http://" + hostPort, "http://" + localhostPort))
            .build();

        HttpServletStreamableServerTransportProvider transport =
            HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(McpJson.mapper())
                .mcpEndpoint("/mcp")
                .securityValidator(validator)
                .build();
        McpSyncServer mcpServer = McpToolRegistry.createHttp(transport).build();

        Server jetty = new Server(new InetSocketAddress("127.0.0.1", port));
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(transport), "/mcp");
        jetty.setHandler(context);

        try {
            jetty.start();
            running.set(new RunningServer(jetty, mcpServer, port));
            return endpoint();
        } catch (Exception e) {
            try {
                mcpServer.closeGracefully();
            } catch (Exception closeError) {
                e.addSuppressed(closeError);
            } finally {
                try {
                    jetty.stop();
                } catch (Exception closeError) {
                    e.addSuppressed(closeError);
                } finally {
                    jetty.destroy();
                }
            }
            throw e;
        }
    }

    public synchronized void stop() {
        RunningServer active = running.getAndSet(null);
        if (active == null) {
            return;
        }
        try {
            active.mcpServer().closeGracefully();
        } catch (Exception e) {
            System.err.println("Unable to close MCP server: " + e.getMessage());
        } finally {
            try {
                active.jetty().stop();
            } catch (Exception e) {
                System.err.println("Unable to stop MCP HTTP server: " + e.getMessage());
            } finally {
                active.jetty().destroy();
            }
        }
    }

    public boolean isRunning() {
        return running.get() != null;
    }

    public String endpoint() {
        RunningServer active = running.get();
        return active == null ? "http://127.0.0.1:" + configuredPort() + "/mcp"
            : "http://127.0.0.1:" + active.port() + "/mcp";
    }

    public static int configuredPort() {
        String value = System.getProperty("glory.mcp.port");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        int port = Integer.parseInt(value.trim());
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("glory.mcp.port must be between 1 and 65535");
        }
        return port;
    }

    private record RunningServer(Server jetty, McpSyncServer mcpServer, int port) {}
}
