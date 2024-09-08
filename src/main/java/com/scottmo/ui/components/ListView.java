package com.scottmo.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ListView extends JPanel {
    private static final Dimension MIN_SIZE = new Dimension(200, 400);
    
    private List<JPanel> itemPanels = new ArrayList<>();
    private List<JCheckBox> checkBoxes = new ArrayList<>();
    private JPanel listPanel = new JPanel();

    private List<String> selectedItems = new ArrayList<>();

    private SelectionListener listener;

    public ListView() {
        setMinimumSize(MIN_SIZE);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Makes scrolling smoother

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    public ListView(List<String> items) {
        this();
        setItems(items);
    }

    public void setItems(List<String> items) {
        listPanel.removeAll(); // Remove all current items
        itemPanels.clear();
        checkBoxes.clear();
        selectedItems.clear();

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
            itemPanel.setBackground(getBackground()); // Use parent background

            // set item selection on single click
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    checkBox.setSelected(!checkBox.isSelected());
                    setSelected(checkBox, itemPanel, true);
                }
            });
            // set item selection on checkbox clicked
            checkBox.addActionListener(e -> {
                setSelected(checkBox, itemPanel, true);
            });

            itemPanels.add(itemPanel);
            listPanel.add(itemPanel);
        }

        // Refresh the panel to show the updated items
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void setSelected(JCheckBox checkBox, JPanel itemPanel, boolean triggerListener) {
        String item = checkBox.getText();
        if (checkBox.isSelected()) {
            selectedItems.add(item);
        } else {
            selectedItems.remove(item);
        }

        updateBackground(itemPanel, checkBox.isSelected());

        if (triggerListener && listener != null) {
            listener.onItemSelected(item, checkBox.isSelected());
        }
    }

    private void updateBackground(JPanel itemPanel, boolean isSelected) {
        itemPanel.setBackground(isSelected ? UIManager.getColor("List.selectionBackground") : getBackground());
    }

    public List<String> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectCount() {
        return selectedItems.size();
    }

    public void selectAll(boolean select) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setSelected(select);
            setSelected(checkBoxes.get(i), itemPanels.get(i), false);
        }
    }

    public interface SelectionListener {
        void onItemSelected(String item, boolean isSelected);
    }

    public ListView setSelectionListener(SelectionListener listener) {
        this.listener = listener;
        return this;
    }
}
