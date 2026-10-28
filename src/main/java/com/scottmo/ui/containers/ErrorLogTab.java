package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.scottmo.config.Labels;
import com.scottmo.core.operations.LogOperations;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.ErrorRegistry;

public final class ErrorLogTab extends JPanel {
    private final JTextArea textArea;
    private final LogOperations logOperations;
    private final ErrorRegistry.Registration listenerRegistration;

    public ErrorLogTab() {
        this(new LogOperations());
    }

    ErrorLogTab(LogOperations logOperations) {
        this.logOperations = logOperations;
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);

        JButton buttonClear = new JButton(Labels.get("errorLog.buttonClear"));
        buttonClear.addActionListener(evt -> clearLog());

        JButton buttonExport = new JButton(Labels.get("errorLog.buttonExport"));
        buttonExport.addActionListener(evt -> exportLog());

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(scrollPane).weightBy(1.0),
            row(UI_GAP,
                cell(buttonClear),
                cell(buttonExport)
            )
        ).getComponent(), BorderLayout.CENTER);

        // Register listener for live updates
        listenerRegistration = ErrorRegistry.addListener(this::appendLog, this::clearText);
    }

    private void appendLog(String logEntry) {
        // Swing operations must run on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            textArea.append(logEntry);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    void clearLog() {
        logOperations.clear();
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                // Drain the clear callback after earlier queued appends; do not clear again here,
                // since a later log could have been appended after the registry clear.
                SwingUtilities.invokeAndWait(() -> {});
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (java.lang.reflect.InvocationTargetException e) {
                Dialog.error("Failed to clear the log view: " + e.getMessage(), e);
            }
        }
    }

    void exportLog() {
        try {
            Path output = logOperations.export();
            Dialog.info("Successfully exported logs to " + output);
        } catch (IOException e) {
            Dialog.error("Failed to export logs: " + e.getMessage(), e);
        }
    }

    String getLogText() {
        return textArea.getText();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        listenerRegistration.close();
    }

    private void clearText() {
        // Queue the clear after already queued log appends, including when called on the EDT.
        SwingUtilities.invokeLater(() -> textArea.setText(""));
    }
}
