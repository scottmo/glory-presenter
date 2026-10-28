package com.scottmo.mcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;

/** Runs the official MCP STDIO transport until the client closes stdin. */
public final class McpStdioServer {
    private McpStdioServer() {}

    public static void run(InputStream input, OutputStream output) throws InterruptedException {
        CountDownLatch eof = new CountDownLatch(1);
        InputStream observedInput = new EofSignalInputStream(input, eof);
        StdioServerTransportProvider transport =
            new StdioServerTransportProvider(McpJson.mapper(), observedInput, output);
        McpSyncServer server = McpToolRegistry.createStdio(transport).build();
        try {
            eof.await();
        } finally {
            try {
                server.closeGracefully();
            } finally {
                transport.closeGracefully().block();
            }
        }
    }

    private static final class EofSignalInputStream extends InputStream {
        private final InputStream delegate;
        private final CountDownLatch eof;
        private boolean signalled;

        private EofSignalInputStream(InputStream delegate, CountDownLatch eof) {
            this.delegate = delegate;
            this.eof = eof;
        }

        @Override
        public int read() throws IOException {
            int value;
            try {
                value = delegate.read();
            } catch (IOException e) {
                signalEof();
                throw e;
            }
            if (value < 0) {
                signalEof();
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int count;
            try {
                count = delegate.read(buffer, offset, length);
            } catch (IOException e) {
                signalEof();
                throw e;
            }
            if (count < 0) {
                signalEof();
            }
            return count;
        }

        private synchronized void signalEof() {
            if (!signalled) {
                signalled = true;
                eof.countDown();
            }
        }
    }
}
