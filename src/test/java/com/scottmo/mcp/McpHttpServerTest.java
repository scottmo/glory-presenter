package com.scottmo.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class McpHttpServerTest {
    private final McpServerManager server = McpServerManager.get();

    @AfterEach
    void stopServer() {
        server.stop();
        System.clearProperty("glory.mcp.port");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void startsOnLoopbackServesMcpAndRejectsUntrustedOriginThenStops() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        System.setProperty("glory.mcp.port", Integer.toString(port));

        String endpoint = server.start();
        assertTrue(server.isRunning());
        assertEquals("http://127.0.0.1:" + port + "/mcp", endpoint);

        assertEquals(endpoint, server.start(), "start is idempotent while the server is running");
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        String initialize = """
            {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"test-client","version":"1"}}}
            """;
        HttpResponse<String> allowedResponse = client.send(
            HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(3))
                .header("Origin", "http://localhost:" + port)
                .header("Accept", "application/json, text/event-stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(initialize))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(200, allowedResponse.statusCode(), allowedResponse.body());
        assertTrue(allowedResponse.body().contains("2025-11-25"), allowedResponse.body());

        String sessionId = allowedResponse.headers().firstValue("Mcp-Session-Id").orElseThrow();
        HttpResponse<String> initializedResponse = post(client, endpoint, port, sessionId,
            "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, initializedResponse.statusCode());
        HttpResponse<String> listResponse = post(client, endpoint, port, sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");
        assertEquals(200, listResponse.statusCode(), listResponse.body());
        assertTrue(listResponse.body().contains("mcp_status"), listResponse.body());
        HttpResponse<String> callResponse = post(client, endpoint, port, sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"mcp_status\",\"arguments\":{}}}");
        assertEquals(200, callResponse.statusCode(), callResponse.body());
        assertTrue(callResponse.body().contains("Glory Presenter"), callResponse.body());

        HttpResponse<String> deniedResponse = client.send(
            HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(3))
                .header("Origin", "https://untrusted.example")
                .header("Accept", "application/json, text/event-stream")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(initialize))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(403, deniedResponse.statusCode(), deniedResponse.body());

        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(3000);
            OutputStream request = socket.getOutputStream();
            String hostileRequest = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{" +
                "\"protocolVersion\":\"2025-11-25\",\"capabilities\":{},\"clientInfo\":{" +
                "\"name\":\"host-test\",\"version\":\"1\"}}}";
            byte[] body = hostileRequest.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            request.write(("POST /mcp HTTP/1.1\r\nHost: attacker.example\r\nOrigin: http://localhost:" + port
                + "\r\nAccept: application/json, text/event-stream\r\nContent-Type: application/json\r\nContent-Length: "
                + body.length + "\r\nConnection: close\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            request.write(body);
            request.flush();
            String responseLine = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
            assertTrue(responseLine.contains(" 421 "),
                "Jetty must reject a mismatched Host before routing MCP, got: " + responseLine);
        }

        server.stop();
        assertFalse(server.isRunning());
        assertThrows(Exception.class, () -> client.send(
            HttpRequest.newBuilder(URI.create(endpoint)).timeout(Duration.ofSeconds(3)).GET().build(),
            HttpResponse.BodyHandlers.ofString()));
        assertEquals(endpoint, server.start(), "the server can restart cleanly after stopping");
        server.stop();

        try (ServerSocket occupiedPort = new ServerSocket(port)) {
            System.setProperty("glory.mcp.port", Integer.toString(port));
            assertThrows(Exception.class, server::start, "starting on an occupied port should fail");
            assertFalse(server.isRunning());
        }
    }

    private static HttpResponse<String> post(HttpClient client, String endpoint, int port,
        String sessionId, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(Duration.ofSeconds(3))
            .header("Origin", "http://localhost:" + port)
            .header("Mcp-Session-Id", sessionId)
            .header("MCP-Protocol-Version", "2025-11-25")
            .header("Accept", "application/json, text/event-stream")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }
}
