package com.scottmo.ui.containers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.scottmo.ui.components.ListView;

public final class SongTab extends JPanel {

    public SongTab() {
        // Create a list of items
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            items.add("Item " + i);
        }

        ListView songList = new ListView(items);
        songList.setSelectionListener(new ListView.SelectionListener() {
            @Override
            public void onItemSelected(String item, boolean isSelected) {
                System.out.println("Item selected: " + item + ", isSelected: " + isSelected);
            }
        });

        // Button to get selected items
        JButton getSelectedButton = new JButton("Get Selected Items");
        getSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedItems = songList.getSelectedItems();
                JOptionPane.showMessageDialog(SongTab.this, "Selected Items: " + selectedItems);
            }
        });

        // Button to select all items
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                songList.selectAll(true);
            }
        });

        // Button to deselect all items
        JButton deselectAllButton = new JButton("Deselect All");
        deselectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                songList.selectAll(false);
            }
        });

        // Add components to the frame
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getSelectedButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);

        setLayout(new BorderLayout());
        add(songList, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
