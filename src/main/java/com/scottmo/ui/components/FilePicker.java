package com.scottmo.ui.components;

import com.scottmo.Application;
import com.scottmo.Config;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class FilePicker {
    public static final int FILES = 0;
    public static final int DIRECTORIES = 1;
    public static final int FILES_AND_DIRECTORIES = 2;
    public static final int FILE_SEARCH = 3;

    private static String selectedFilePath = "";

    public void show(Consumer<String> onSelected) {
        show(FILES_AND_DIRECTORIES, Config.get().dataDir(), onSelected);
    }

    public static void show(int mode, String dirPath, Consumer<String> onSelected) {

        if (mode != FILE_SEARCH) {
            showSystemPicker(mode, dirPath, onSelected);
            return;
        }

        // components
        var modalDialog = new JDialog(Application.get(), "Select File", Dialog.ModalityType.DOCUMENT_MODAL);
        var searchInput = new JTextField();
        var fileList = new JList<String>();
        var selectBtn = new JButton("Select");
        var cancelBtn = new JButton("Cancel");

        // view
        searchInput.setColumns(20);

        fileList.setFixedCellHeight(16);
        fileList.setFixedCellWidth(400);
        fileList.setVisibleRowCount(10);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int appHeight = Application.get().getHeight();
        int appWidth = Application.get().getWidth();
        modalDialog.setBounds(appWidth / 4, appHeight / 4, appWidth / 2, appHeight / 2);

        var modalDialogContent = modalDialog.getContentPane();
        modalDialogContent.setLayout(new MigLayout("wrap 5"));
        modalDialogContent.add(new JLabel("Search"));
        modalDialogContent.add(searchInput, "span, align left");
        modalDialogContent.add(new JScrollPane(fileList), "span");
        modalDialogContent.add(selectBtn);
        modalDialogContent.add(cancelBtn);

        // controls
        List<String> fileNames = listFiles(dirPath);
        fileList.setListData(new Vector<>(fileNames));

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                selectBtn.setEnabled(true);
                selectedFilePath = fileList.getSelectedValue();
            }
        });

        searchInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) return;

                String searchText = searchInput.getText().toLowerCase();
                fileList.setListData(new Vector<>(fileNames.stream()
                        .filter(name -> name.toLowerCase().contains(searchText))
                        .collect(Collectors.toList())));
            }
        });

        selectBtn.setEnabled(false);
        selectBtn.addActionListener(e -> {
            onSelected.accept(Path.of(dirPath, selectedFilePath).toString());
            modalDialog.dispose();
        });

        cancelBtn.addActionListener(e -> {
            modalDialog.dispose();
        });

        modalDialog.setVisible(true);
    }

    private static List<String> listFiles(String dataPath) {
        File[] files = new File(Path.of(dataPath).toString()).listFiles();
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    private static void showSystemPicker(int mode, String defaultPath, Consumer<String> onSelected) {
        var startPath = Path.of(defaultPath).toFile();
        if (!startPath.isDirectory()) {
            startPath = startPath.getParentFile();
        }
        var fc = new JFileChooser();
        fc.setCurrentDirectory(startPath);
        fc.setFileSelectionMode(mode);
        var returnVal = fc.showSaveDialog(Application.get());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            var selectedPath = fc.getSelectedFile().toPath().toAbsolutePath().toString();
            onSelected.accept(selectedPath);
        }
    }
}
