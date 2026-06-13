package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.scottmo.config.Labels;
import com.scottmo.ui.utils.Dialog;
import com.scottmo.ui.utils.ErrorRegistry;

public final class ErrorLogTab extends JPanel {
    private final JTextArea textArea;

    public ErrorLogTab() {
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
        ErrorRegistry.registerListener(this::appendLog);
    }

    private void appendLog(String logEntry) {
        // Swing operations must run on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            textArea.append(logEntry);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    void clearLog() {
        ErrorRegistry.clear();
        textArea.setText("");
    }

    void exportLog() {
        try {
            List<String> logs = ErrorRegistry.getLogs();
            Files.write(Path.of("error.log"), logs, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Dialog.info("Successfully exported logs to error.log");
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
        ErrorRegistry.unregisterListener();
    }
}
