package com.scottmo.ui.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchableListDialog extends JDialog {

    private final JTextField searchField;
    private final JList<String> list;
    private final DefaultListModel<String> listModel;
    private final List<String> allItems;
    private final Consumer<String> onSelect;

    public SearchableListDialog(Window owner, String title, List<String> items, Consumer<String> onSelect) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.allItems = new ArrayList<>(items);
        this.onSelect = onSelect;

        setLayout(new BorderLayout(5, 5));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search Field
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    list.requestFocusInWindow();
                    list.setSelectedIndex(0);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     if (listModel.getSize() > 0) {
                         list.setSelectedIndex(0);
                         confirmSelection();
                     }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        // List
        listModel = new DefaultListModel<>();
        items.forEach(listModel::addElement);
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmSelection();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> confirmSelection());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 500);
        setLocationRelativeTo(owner);
        
        // Close on Esc
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void filter() {
        String term = searchField.getText().toLowerCase();
        listModel.clear();
        List<String> filtered = allItems.stream()
                .filter(item -> item.toLowerCase().contains(term))
                .collect(Collectors.toList());
        filtered.forEach(listModel::addElement);
    }

    private void confirmSelection() {
        String selected = list.getSelectedValue();
        if (selected != null) {
            onSelect.accept(selected);
            dispose();
        }
    }

    public static void show(Window owner, String title, List<String> items, Consumer<String> onSelect) {
        new SearchableListDialog(owner, title, items, onSelect).setVisible(true);
    }
}
