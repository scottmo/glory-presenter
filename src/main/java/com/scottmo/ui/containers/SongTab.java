package com.scottmo.ui.containers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.shared.Pair;
import com.scottmo.ui.components.ListView;

public final class SongTab extends JPanel {
    private ConfigService configService = ConfigService.get();
    private SongService songService = ServiceProvider.get(SongService.class).get();

    // cache to look up song id
    private Map<String, Integer> songIdMap = new HashMap<>();

    private ListView songList = new ListView();

    public SongTab() {
        loadSongList();
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

    private void loadSongList() {
        List<Pair<Integer, String>> songs = songService.getAllSongDescriptors(configService.getConfig().getLocales());
        List<String> items = new ArrayList<>();
        songIdMap.clear();
        for (Pair<Integer,String> pair : songs) {
            Integer songId = pair.key();
            String songName = pair.value();
            songIdMap.put(songName, songId);
            items.add(songName);
        }
        songList.setItems(items);
    }
}
