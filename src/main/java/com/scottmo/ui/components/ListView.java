package com.scottmo.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class ListView extends JPanel {
    private static final Color DEFAULT_BACKGROUND = Color.WHITE;
    private static final Color SELECTED_BACKGROUND = new Color(173, 216, 230); // light blue
    
    private List<JPanel> itemPanels;
    private List<JCheckBox> checkBoxes;
    private JPanel listPanel;
    private SelectionListener listener;

    public ListView(List<String> items) {
        itemPanels = new ArrayList<>();
        checkBoxes = new ArrayList<>();

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (String item : items) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), // Border between items
                new EmptyBorder(5, 5, 5, 5) // Padding inside the panel
            ));

            JCheckBox checkBox = new JCheckBox(item);
            checkBox.setOpaque(false); // Make checkbox background transparent
            checkBoxes.add(checkBox);

            itemPanel.add(checkBox, BorderLayout.WEST);
            itemPanel.setBackground(DEFAULT_BACKGROUND); // Default background

            // set item selection on single click
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    checkBox.setSelected(!checkBox.isSelected());
                    setSelected(item, checkBox, itemPanel);
                }
            });
            // set item selection on checkbox clicked
            checkBox.addActionListener(e -> {
                setSelected(item, checkBox, itemPanel);
            });

            itemPanels.add(itemPanel);
            listPanel.add(itemPanel);
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Makes scrolling smoother

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setSelected(String item, JCheckBox checkBox, JPanel itemPanel) {
        updateBackground(itemPanel, checkBox.isSelected());
        if (listener != null) {
            listener.onItemSelected(item, checkBox.isSelected());
        }
    }

    private void updateBackground(JPanel itemPanel, boolean isSelected) {
        itemPanel.setBackground(isSelected ? SELECTED_BACKGROUND : DEFAULT_BACKGROUND);
    }

    public List<String> getSelectedItems() {
        List<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selectedItems.add(checkBoxes.get(i).getText());
            }
        }
        return selectedItems;
    }

    public void selectAll(boolean select) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setSelected(select);
            updateBackground(itemPanels.get(i), select);
        }
    }

    public interface SelectionListener {
        void onItemSelected(String item, boolean isSelected);
    }

    public void setSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }
}
