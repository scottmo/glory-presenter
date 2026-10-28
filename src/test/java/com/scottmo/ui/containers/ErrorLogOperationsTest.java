package com.scottmo.ui.containers;

import com.scottmo.core.operations.LogOperations;
import com.scottmo.ui.utils.ErrorRegistry;
import com.scottmo.ui.utils.InMemoryAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.SwingUtilities;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ErrorLogOperationsTest {
    @TempDir Path tempDir;

    private List<String> previousLogs;
    private Logger logger;
    private boolean previousAdditivity;
    private final List<org.apache.log4j.Appender> previousAppenders = new ArrayList<>();
    private InMemoryAppender appender;
    private final AtomicReference<ErrorLogTab> tab = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        previousLogs = ErrorRegistry.getLogs();
        ErrorRegistry.clear();
        logger = Logger.getLogger("com.scottmo.ErrorLogOperationsTest." + System.nanoTime());
        previousAdditivity = logger.getAdditivity();
        Enumeration<?> appenders = logger.getAllAppenders();
        while (appenders.hasMoreElements()) {
            org.apache.log4j.Appender existing = (org.apache.log4j.Appender) appenders.nextElement();
            previousAppenders.add(existing);
            logger.removeAppender(existing);
        }
        logger.setAdditivity(false);
        appender = new InMemoryAppender();
        appender.setLayout(new PatternLayout("%m%n"));
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tab.get() != null) {
            SwingUtilities.invokeAndWait(() -> tab.get().removeNotify());
        }
        if (logger != null) {
            logger.removeAppender(appender);
            for (org.apache.log4j.Appender existing : previousAppenders) {
                logger.addAppender(existing);
            }
            logger.setAdditivity(previousAdditivity);
        }
        if (appender != null) {
            appender.close();
        }
        ErrorRegistry.clear();
        for (String previousLog : previousLogs) {
            ErrorRegistry.log(previousLog);
        }
    }

    @Test
    void readsAndExportsActualLog4jEntriesAsUtf8InCurrentOrder() throws Exception {
        LogOperations operations = new LogOperations(tempDir.resolve("unused-default.log"));
        logger.error("First error");
        logger.error("Second error – café");

        List<String> all = operations.read(null);
        assertTrue(all.get(0).contains("First error"));
        assertTrue(all.get(1).contains("Second error – café"));
        assertEquals(List.of(all.get(1)), operations.read(1));

        Path exported = operations.export(tempDir.resolve("logs/exported.log").toString());
        assertEquals("First error" + System.lineSeparator() + "Second error – café" + System.lineSeparator(),
            Files.readString(exported, StandardCharsets.UTF_8));
    }

    @Test
    void clearFromOperationsClearsOpenUiOnTheEventDispatchThread() throws Exception {
        LogOperations operations = new LogOperations(tempDir.resolve("error.log"));
        SwingUtilities.invokeAndWait(() -> tab.set(new ErrorLogTab(operations)));
        logger.error("Visible before clear");
        flushEdt();
        assertTrue(tab.get().getLogText().contains("Visible before clear"));

        assertEquals(1, operations.clear());
        flushEdt();
        assertEquals("", tab.get().getLogText());
        assertTrue(ErrorRegistry.getLogs().isEmpty());
        assertFalse(Files.exists(tempDir.resolve("error.log")), "Clearing memory must not create an export file");
    }

    private static void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {});
    }
}
