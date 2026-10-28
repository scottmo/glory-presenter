package com.scottmo.core.operations;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** Serializes shared application operations across Swing callbacks and MCP transports. */
public final class ApplicationExecutionLock {
    private static final ApplicationExecutionLock SHARED = new ApplicationExecutionLock();

    public static ApplicationExecutionLock shared() {
        return SHARED;
    }

    private final ReentrantLock lock = new ReentrantLock(true);

    public <T> T call(Supplier<T> operation) {
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }

    public void run(Runnable operation) {
        lock.lock();
        try {
            operation.run();
        } finally {
            lock.unlock();
        }
    }

    public <T> T io(IOSupplier<T> operation) throws IOException {
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }

    @FunctionalInterface
    public interface IOSupplier<T> {
        T get() throws IOException;
    }
}
