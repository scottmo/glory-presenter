package com.scottscmo.ui.components;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * Component builder helper
 */
public class C {
    // split pane
    private static JComponent splitPane(int orientation, JComponent ... components) {
        JComponent tail = scroll(components[0]);
        for (int i = 1; i < components.length; i++) {
            tail = new JSplitPane(orientation, tail, scroll(components[i]));
        }
        return tail;
    }

    public static JComponent splitH(JComponent ... components) {
        return splitPane(JSplitPane.HORIZONTAL_SPLIT, components);
    }

    public static JComponent splitV(JComponent ... components) {
        return splitPane(JSplitPane.VERTICAL_SPLIT, components);
    }

    // scroll pane
    public static JComponent scroll(JComponent component) {
        return new JScrollPane(component);
    }
}
