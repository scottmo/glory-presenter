package com.scottmo.ui.containers;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.scottmo.config.Labels;
import com.scottmo.mcp.McpServerManager;

/** Child-process UI harness isolated from the test runner's global config and server singleton. */
public final class ConfigsTabMcpLifecycleHarness {
    private static final McpServerManager SERVER = McpServerManager.get();
    private static final AtomicReference<JButton> TOGGLE = new AtomicReference<>();
    private static final AtomicReference<JLabel> STATUS = new AtomicReference<>();

    private ConfigsTabMcpLifecycleHarness() {}

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            System.setProperty("glory.mcp.port", Integer.toString(port));
            SwingUtilities.invokeAndWait(() -> {
                ConfigsTab tab = new ConfigsTab();
                TOGGLE.set(findButton(tab, Labels.get("configs.mcpStart")));
                STATUS.set(findStatus(tab));
                if (TOGGLE.get() == null || STATUS.get() == null) {
                    throw new AssertionError("Configuration tab MCP controls were not found");
                }
                if (!STATUS.get().getText().startsWith(Labels.get("configs.mcpStopped"))) {
                    throw new AssertionError("MCP server should start stopped: " + STATUS.get().getText());
                }
                TOGGLE.get().doClick();
            });

            State running = awaitState(true, Labels.get("configs.mcpStop"));
            if (!running.status().startsWith(Labels.get("configs.mcpRunning"))
                || !running.status().contains("http://127.0.0.1:" + port + "/mcp")) {
                throw new AssertionError("Configuration tab did not report its running endpoint: " + running);
            }

            SwingUtilities.invokeAndWait(() -> TOGGLE.get().doClick());
            State stopped = awaitState(false, Labels.get("configs.mcpStart"));
            if (!stopped.status().startsWith(Labels.get("configs.mcpStopped"))) {
                throw new AssertionError("Configuration tab did not report stopped state: " + stopped);
            }
            System.out.println("CONFIG_TAB_MCP_LIFECYCLE_OK");
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(1);
        } finally {
            SERVER.stop();
        }
        System.exit(0);
    }

    private static State awaitState(boolean expectedRunning, String expectedButtonText) throws Exception {
        State latest = null;
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(12);
        while (System.nanoTime() < deadline) {
            latest = readState();
            if (latest.running() == expectedRunning && latest.enabled()
                && expectedButtonText.equals(latest.buttonText())) {
                return latest;
            }
            Thread.sleep(40);
        }
        throw new AssertionError("Timed out waiting for Configurations tab state; latest=" + latest);
    }

    private static State readState() throws Exception {
        AtomicReference<State> state = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> state.set(new State(SERVER.isRunning(), TOGGLE.get().isEnabled(),
            TOGGLE.get().getText(), STATUS.get().getText())));
        return state.get();
    }

    private static JButton findButton(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container container) {
                JButton nested = findButton(container, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static JLabel findStatus(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof JLabel label
                && (label.getText().startsWith(Labels.get("configs.mcpStopped"))
                    || label.getText().startsWith(Labels.get("configs.mcpRunning")))) {
                return label;
            }
            if (component instanceof Container container) {
                JLabel nested = findStatus(container);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private record State(boolean running, boolean enabled, String buttonText, String status) {}
}
