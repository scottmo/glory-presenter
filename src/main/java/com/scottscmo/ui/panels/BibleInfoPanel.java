package com.scottscmo.ui.panels;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottscmo.Config;
import com.scottscmo.bibleMetadata.BibleMetadata;
import com.scottscmo.model.bible.BibleModel;
import com.scottscmo.ui.components.Form;
import com.scottscmo.ui.components.FormInput;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class BibleInfoPanel extends JPanel {
    public BibleInfoPanel() {
        var bookIds = String.join(", ", BibleMetadata.getBookIdsInOrder());

        var bookIdsDisplayText = new JTextArea(bookIds);
        bookIdsDisplayText.setColumns(85);
        bookIdsDisplayText.setEditable(false);
        bookIdsDisplayText.setLineWrap(true);
        bookIdsDisplayText.setWrapStyleWord(true);

        var dataPathKey = "dataFilePath";
        var versionKey = "version";
        var bibleImportForm = new Form("Bible Importer", List.of(
            new FormInput(dataPathKey, "Input JSON", "file", Config.getRelativePath("bible.json")),
            new FormInput(versionKey, "Version", "text", "niv")
        ), (form) -> importBible(form.getValue(versionKey), form.getValue(dataPathKey)));

        setLayout(new MigLayout("", "left", "top"));
        add(new JLabel("Available versions: " + getAvailableBibleVersion()), "wrap");
        add(new JLabel("Book IDs: "), "wrap");
        add(bookIdsDisplayText, "span, wrap");
        add(bibleImportForm.getUI(), "wrap");
    }

    private String getAvailableBibleVersion() {
        try {
            return String.join(", ", BibleModel.get().getAvailableVersions());
        } catch (SQLException e) {
            return "none";
        }
    }

    private String importBible(String version, String bibleJSONPath) {
        try {
            var bibleJsonTypeRef = new TypeReference<Map<String, List<List<String>>>>() {
            };
            var bibleJson = new ObjectMapper().readValue(new File(bibleJSONPath), bibleJsonTypeRef);
            int insertedVerseCount = BibleModel.get().insert(bibleJson, version);
            return "Successfully inserted %d %s bible verses".formatted(insertedVerseCount, version);
        } catch (IOException e) {
            return "Failed to load bible";
        }
    }
}
