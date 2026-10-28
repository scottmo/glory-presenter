package com.scottmo.ui.containers;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.operations.ConfigOperations;
import com.scottmo.mcp.McpServerManager;
import com.scottmo.ui.components.JsonEditor;

public final class ConfigsTab extends JPanel {
    public ConfigsTab() {
        ConfigOperations configOperations = ConfigOperations.forApplication();
        configOperations.snapshotJson();
        Path configPath = ConfigService.get().getConfigPath();
        JsonEditor jsonEditor = new JsonEditor(configPath, Labels.get("configs.settingsEditorTitle"));
        jsonEditor.setOnSave(configOperations::saveJson);
        jsonEditor.setOnReload(() -> {
            configOperations.reload();
            return configOperations.snapshotJson();
        });

        JButton buttonToggleDarkMode = new JButton(Labels.get("configs.buttonToggleDarkMode"));
        buttonToggleDarkMode.addActionListener(evt -> {
            boolean darkMode = !ConfigService.get().getConfig().isDarkMode();
            configOperations.setDarkMode(darkMode);
            jsonEditor.reloadFromSource();
        });

        JLabel mcpStatus = new JLabel();
        JButton toggleMcp = new JButton();
        Runnable refreshMcpStatus = () -> {
            boolean running = McpServerManager.get().isRunning();
            mcpStatus.setText(running
                ? Labels.get("configs.mcpRunning") + " " + McpServerManager.get().endpoint()
                : Labels.get("configs.mcpStopped") + " " + McpServerManager.get().endpoint());
            toggleMcp.setText(Labels.get(running ? "configs.mcpStop" : "configs.mcpStart"));
            toggleMcp.setEnabled(true);
        };
        toggleMcp.addActionListener(evt -> runMcpLifecycle(mcpStatus, toggleMcp, refreshMcpStatus));
        refreshMcpStatus.run();

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            row(UI_GAP,
                cell(buttonToggleDarkMode)
            ),
            row(UI_GAP,
                cell(new JLabel(Labels.get("configs.mcpTitle"))),
                cell(toggleMcp),
                cell(mcpStatus).weightBy(1.0)
            ),
            cell(jsonEditor).weightBy(1.0)
        ).getComponent());
    }

    private static void runMcpLifecycle(
        JLabel status,
        JButton toggle,
        Runnable refresh
    ) {
        boolean shouldStart = !McpServerManager.get().isRunning();
        toggle.setEnabled(false);
        status.setText(Labels.get(shouldStart ? "configs.mcpStarting" : "configs.mcpStopping"));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (shouldStart) {
                    McpServerManager.get().start();
                } else {
                    McpServerManager.get().stop();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refresh.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText(Labels.get("configs.mcpFailed") + " " + e.getMessage());
                    toggle.setEnabled(true);
                    toggle.setText(Labels.get(shouldStart ? "configs.mcpStart" : "configs.mcpStop"));
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    status.setText(Labels.get("configs.mcpFailed") + " " + cause.getMessage());
                    toggle.setEnabled(true);
                    toggle.setText(Labels.get(shouldStart ? "configs.mcpStart" : "configs.mcpStop"));
                }
            }
        }.execute();
    }
}
