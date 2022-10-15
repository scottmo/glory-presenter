package com.scottscmo.ui.panels;

import com.scottscmo.AppLogger;
import com.scottscmo.Config;
import com.scottscmo.bibleReference.BibleReference;
import com.scottscmo.google.Action;
import com.scottscmo.google.GoogleSlidesService;
import com.scottscmo.model.song.converters.KVMDConverter;
import com.scottscmo.ui.components.Form;
import com.scottscmo.ui.components.FormInput;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class GSlidesPanel extends JPanel {
    private final JTextField slideUrlInput = new JTextField();
    private final JTextField insertionIndexInput = new JTextField("0");

    public GSlidesPanel() {
        var googleService = new GoogleSlidesService();

        setLayout(new MigLayout());

        // ppt id
        add(new JLabel("Google Slides URL/ID"));
        add(slideUrlInput, "growx, wrap");

        // ppt slide insertion index
        add(new JLabel("Insertion Index"));
        add(insertionIndexInput, "growx, wrap");

        // basic actions
        var setDefaultTitleTextBtn = new JButton("Set Default Title Text");
        add(setDefaultTitleTextBtn, "wrap");
        setDefaultTitleTextBtn.addActionListener(evt -> {
            try {
                googleService.setDefaultTitleText(getPresentationId());
            } catch (IOException e) {
                AppLogger.showError("Action failed!", e);
            }
        });

        var setBaseFontBtn = new JButton("Set Base Font");
        add(setBaseFontBtn, "wrap");
        setBaseFontBtn.addActionListener(evt -> {
            try {
                googleService.setBaseFont(getPresentationId());
            } catch (IOException e) {
                AppLogger.showError("Action failed!", e);
            }
        });

        // bible verses
        var bibleSlidesGeneratorForm = new Form("Bible Slides Generator", Map.of(
                "verses"   , new FormInput("Verses", "text", "john 1:2-5,7-8"),
                "versions" , new FormInput("Bible Versions", "text", "cuv,niv")
                ), form -> {
            var bibleRef = new BibleReference(form.getValue("versions") + " - " + form.getValue("verses"));
            try {
                googleService.insertBibleText(getPresentationId(), bibleRef, getInsertionIndex());
                return "Bible slides have been successfully generated!";
            } catch (IOException e) {
                AppLogger.showError("Action failed!", e);
            }
            return null;
        });
        add(bibleSlidesGeneratorForm.getUI(), "span, wrap");

        var songSlidesGeneratorForm = new Form("Song Slides Generator", Map.of(
                "song" , new FormInput("Song", "fileSearch", Config.getRelativePath(Config.SONG_SLIDES_DIR))
        ), form -> {
            try {
                var slideSong = Files.readString(Path.of(form.getValue("song")));
                var song = KVMDConverter.parse(slideSong);
                if (song != null) {
                    googleService.insertSong(getPresentationId(), song, getInsertionIndex());
                    return "Song slides have been successfully generated!";
                }
            } catch (IOException e) {
                AppLogger.showError("Unable to generate song slides!", e);
            }
            return null;
        });
        add(songSlidesGeneratorForm.getUI(), "span, wrap");

        var genericSlidesGeneratorForm = new Form("Generate from template", Map.of(
                "title"      , new FormInput("Title", "text"),
                "folderId"   , new FormInput("Folder ID", "text"),
                "templateId" , new FormInput("Template ID", "text"),
                "inserts"    , new FormInput("Inserts", "textarea")
        ), form -> {
            List<Action> inserts = parseSlideInserts(form.getValue("inserts"));
            String presentationId = googleService.copyPresentation(form.getValue("title"), form.getValue("folderId"), form.getValue("templateId"));
            googleService.generateSlides(presentationId, inserts);
            return "Generated!";
        });
        add(genericSlidesGeneratorForm.getUI(), "span, wrap");
    }

    private String getPresentationId() {
        String input = slideUrlInput.getText();
        if (input.contains("/")) {
            try {
                var url = new URL(input);
                return url.getPath().substring(url.getPath().indexOf("/d/") + 3, url.getPath().lastIndexOf("/"));
            } catch (MalformedURLException e) {
                AppLogger.showError("Unable to extract Google slide ID", e);
            }
        }
        return input;
    }

    private int getInsertionIndex() {
        return Integer.parseInt(insertionIndexInput.getText());
    }

    private List<Action> parseSlideInserts(String inserts) {
        try {
            return Arrays.stream(inserts.split("\n")).map(insert -> {
                String[] indexAction = insert.split(":", 2);
                int index = Integer.parseInt(indexAction[0].trim());
                String action = indexAction[1].trim();
                String[] typeInput = action.split("/", 2);
                String type = typeInput[0].trim();
                String input = typeInput[1].trim();
                return new Action(type, index, input);
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Invalid inserts!", e);
        }
    }
}
