package com.scottmo.ui.utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class UndoRedoHelper {

    /**
     * Attaches cross-platform Undo/Redo capability to any Swing text component.
     * Uses Ctrl+Z/Y on Windows/Linux and Cmd+Z/Y on macOS.
     */
    public static void attachUndoManager(JTextComponent component) {
        UndoManager undoManager = new UndoManager();

        // 1. Listen for document changes and push them to the history stack
        component.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
        });

        // 2. Fetch the native OS shortcut modifier (Ctrl on Win/Linux, Cmd on Mac)
        int modifierMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // 3. Define OS-agnostic KeyStrokes
        KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifierMask);
        KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifierMask);

        // 4. Map the inputs when the component is focused
        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(undoKey, "UndoAction");
        inputMap.put(redoKey, "RedoAction");

        // 5. Bind the actions to execute the Undo/Redo commands
        actionMap.put("UndoAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    } else {
                        component.getToolkit().beep(); // Alert if stack is empty
                    }
                } catch (CannotUndoException ex) {
                    component.getToolkit().beep();
                }
            }
        });

        actionMap.put("RedoAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    } else {
                        component.getToolkit().beep(); // Alert if no actions to redo
                    }
                } catch (CannotRedoException ex) {
                    component.getToolkit().beep();
                }
            }
        });
    }

    public static void attachUndoManager(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextComponent) {
                attachUndoManager((JTextComponent) comp);
            } else if (comp instanceof Container) {
                attachUndoManager((Container) comp);
            }
        }
    }
}
