package com.scottmo.ui.containers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scottmo.ui.utils.ErrorRegistry;

class ErrorLogTabTest {
    private static final Path LOG_PATH = Path.of("error.log");
    private static final Path BACKUP_PATH = Path.of("error.log.bak");
    private boolean backupExists = false;

    @BeforeEach
    void setUp() throws IOException {
        com.scottmo.ui.utils.Dialog.showDialogs = false;
        // Backup existing error.log
        if (Files.exists(LOG_PATH)) {
            Files.copy(LOG_PATH, BACKUP_PATH, StandardCopyOption.REPLACE_EXISTING);
            backupExists = true;
        }
        Files.deleteIfExists(LOG_PATH);
        
        // Clear in-memory logs
        ErrorRegistry.clear();
    }

    @AfterEach
    void tearDown() throws IOException {
        com.scottmo.ui.utils.Dialog.showDialogs = true;
        // Unregister any leftover listeners to avoid test interference
        ErrorRegistry.unregisterListener();
        ErrorRegistry.clear();
        
        // Delete test log file
        Files.deleteIfExists(LOG_PATH);
        // Restore backup
        if (backupExists) {
            Files.move(BACKUP_PATH, LOG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void testLoggingToAppenderUpdatesUIAndExportAndClear() throws Exception {
        // Create tab (registers listener)
        ErrorLogTab tab = new ErrorLogTab();

        // 1. Initially empty
        assertEquals("", tab.getLogText().trim());

        // 2. Log via Log4j and check in-memory propagation
        Logger logger = Logger.getLogger(ErrorLogTabTest.class);
        logger.error("Test error message");

        // Wait a brief moment for SwingUtilities.invokeLater to complete on the EDT
        int retries = 20;
        while (retries > 0 && !tab.getLogText().contains("Test error message")) {
            Thread.sleep(50);
            retries--;
        }

        assertTrue(tab.getLogText().contains("Test error message"), "UI log panel should contain the logged error message.");
        assertFalse(Files.exists(LOG_PATH), "Log file should NOT exist yet (no auto export).");

        // 3. Export to log file and verify
        tab.exportLog();
        assertTrue(Files.exists(LOG_PATH), "Log file should exist after export.");
        String fileContent = Files.readString(LOG_PATH);
        assertTrue(fileContent.contains("Test error message"), "Exported log file should contain the message.");

        // 4. Clear UI and registry, and verify it does NOT delete/clear the file
        tab.clearLog();
        assertEquals("", tab.getLogText().trim());
        assertTrue(ErrorRegistry.getLogs().isEmpty());
        assertTrue(Files.exists(LOG_PATH), "Log file should still exist after clearing the panel.");
    }
}
