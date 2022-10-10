package com.scottscmo.ui

import com.scottscmo.Application
import com.scottscmo.Config
import net.miginfocom.swing.MigLayout
import java.awt.Dialog
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*
import javax.swing.*

object FilePicker {
    fun show(mode: String = "fileAndDirectory", dirPath: String = Config.get().dataDir(),
             onSelected: (filePath: String) -> Unit) {

        if (mode != "filelist") {
            showSystemPicker(mode, dirPath, onSelected)
            return
        }

        val appHeight = Application.get().height
        val appWidth = Application.get().width

        val searchInput = JTextField()
        val fileList = JList(emptyArray<String>())
        val selectBtn = JButton("Select")
        val cancelBtn = JButton("Cancel")
        var selectedFilePath = ""

        val modalDialog = JDialog(Application.get(), "Select File", Dialog.ModalityType.DOCUMENT_MODAL)
        modalDialog.contentPane.apply {
            layout = MigLayout("wrap 5")
            add(JLabel("Search"))
            add(searchInput.apply {
                columns = 20
            }, "span, align left")
            add(JScrollPane(fileList.apply {
                fixedCellHeight = 16
                fixedCellWidth = 400
                visibleRowCount = 10
                selectionMode = ListSelectionModel.SINGLE_SELECTION
            }), "span")
            add(selectBtn)
            add(cancelBtn)
        }

        val fileNames = listFiles(dirPath)
        fileList.setListData(fileNames.toTypedArray())

        fileList.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(me: MouseEvent) {
                selectBtn.isEnabled = true
                selectedFilePath = fileList.selectedValue
            }
        })

        searchInput.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ENTER) return

                val searchText = searchInput.text.lowercase(Locale.getDefault())
                fileList.setListData(fileNames
                    .filter { name -> name.lowercase(Locale.getDefault()).contains(searchText) }
                    .toTypedArray())
            }
        })

        selectBtn.isEnabled = false
        selectBtn.addActionListener {
            onSelected(Path.of(dirPath, selectedFilePath).toString())
            modalDialog.dispose()
        }

        cancelBtn.addActionListener {
            modalDialog.dispose()
        }

        modalDialog.setBounds(appWidth / 4, appHeight / 4, appWidth / 2, appHeight / 2)
        modalDialog.isVisible = true
    }

    private fun listFiles(dataPath: String): List<String> {
        return try {
            File(Path.of(dataPath).toString()).listFiles()
                ?.map { f -> f.name }
                ?.sorted()
                ?: emptyList()
        } catch (e: IOException) {
            System.err.println(e.message)
            listOf("Unable to load files!")
        }
    }

    private fun showSystemPicker(mode: String = "fileAndDirectory", defaultPath: String = Config.get().dataDir(),
                                 onSelected: (filePath: String) -> Unit) {
        var startPath = Path.of(defaultPath).toFile()
        if (!startPath.isDirectory) {
            startPath = startPath.parentFile
        }
        val fc = JFileChooser().apply {
            currentDirectory = startPath
            fileSelectionMode = when (mode) {
                "file" -> JFileChooser.FILES_ONLY
                "directory" -> JFileChooser.DIRECTORIES_ONLY
                else -> JFileChooser.FILES_AND_DIRECTORIES
            }
        }
        val returnVal = fc.showSaveDialog(Application.get())
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val selectedPath = fc.selectedFile.toPath().toAbsolutePath().toString()
            onSelected(selectedPath)
        }
    }
}
