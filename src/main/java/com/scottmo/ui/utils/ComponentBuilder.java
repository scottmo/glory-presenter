package com.scottmo.ui.utils;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.scottmo.config.Labels;

/**
 * Component builder helper
 */
public final class ComponentBuilder {
    private static JComponent splitPane(int orientation, JComponent ... components) {
        JComponent tail = new JScrollPane(components[0]);
        for (int i = 1; i < components.length; i++) {
            tail = new JSplitPane(orientation, tail, new JScrollPane(components[i]));
        }
        return tail;
    }

    public static JComponent resizableHBox(JComponent ... components) {
        return splitPane(JSplitPane.HORIZONTAL_SPLIT, components);
    }

    public static JComponent resizableVBox(JComponent ... components) {
        return splitPane(JSplitPane.VERTICAL_SPLIT, components);
    }

    public static JComponent namedLabel(String name) {
        return namedLabel(name, null);
    }

    public static JComponent namedLabel(String name, String styles) {
        return label(Labels.get(name), styles);
    }

    public static JComponent label(String text) {
        return label(text, "wrap");
    }

    public static JComponent label(String text, String styles) {
        var label = new JLabel(text);
        if (styles == null) {
            return label;
        }

        if (styles.contains("wrap")) {
            label.setText("<html>" + text +"</html>");;
        }
        if (styles.contains("bold")) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        }
        return label;
    }
}
