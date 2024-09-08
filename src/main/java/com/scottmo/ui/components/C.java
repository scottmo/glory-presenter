package com.scottmo.ui.components;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

/**
 * Component builder helper
 */
public final class C {
    private JComponent splitPane(int orientation, JComponent ... components) {
        JComponent tail = new JScrollPane(components[0]);
        for (int i = 1; i < components.length; i++) {
            tail = new JSplitPane(orientation, tail, new JScrollPane(components[i]));
        }
        return tail;
    }

    JComponent resizableHBox(JComponent ... components) {
        return splitPane(JSplitPane.HORIZONTAL_SPLIT, components);
    }

    JComponent resizableVBox(JComponent ... components) {
        return splitPane(JSplitPane.VERTICAL_SPLIT, components);
    }
}
