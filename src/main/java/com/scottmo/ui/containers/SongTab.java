package com.scottmo.ui.containers;

import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.shared.Pair;
import com.scottmo.ui.components.ListView;

public final class SongTab extends JPanel {
    private static final int UI_GAP = 5;

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

        setLayout(new BorderLayout());
        add(row(UI_GAP,
            strut(UI_GAP),
            column(UI_GAP,
                strut(UI_GAP),
                cell(new JTextField()),
                cell(songList)
                    .weightBy(1.0), // take as much space as possible
                strut(UI_GAP)
            ).weightBy(4.0),
            column(UI_GAP,
                strut(30),
                cell(getSelectedButton),
                cell(selectAllButton),
                cell(deselectAllButton)
            ).weightBy(1.0),
            strut(UI_GAP)
        )
        .getComponent());
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
        Collections.sort(items);
        songList.setItems(items);
    }
}
