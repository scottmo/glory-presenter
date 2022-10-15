package com.scottscmo.ui.panels;

import com.scottscmo.AppLogger;
import com.scottscmo.Config;
import com.scottscmo.ppt.BibleSlidesGenerator;
import com.scottscmo.ppt.PPTXGenerators;
import com.scottscmo.ui.components.Form;
import com.scottscmo.ui.components.FormInput;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.io.IOException;
import java.util.Map;

public final class PPTXGeneratorsPanel extends JPanel {
    public PPTXGeneratorsPanel() {
        // common fields
        var dataPathKey = "dataFilePath";
        var templatePathKey = "tmplFilePath";
        var outputDirKey = "outputDirPath";

        // common field defaults
        var outputDirDefault = new FormInput("Output Folder", "directory", Config.getRelativePath("../output"));

        var pptGenerator = new Form("PPTX Generator", Map.of(
                dataPathKey, new FormInput("Input File", "file", Config.getRelativePath(".")),
                templatePathKey, new FormInput("Template File", "file", Config.getRelativePath(".")),
                outputDirKey, outputDirDefault
        ), form -> {
            try {
                PPTXGenerators.generate(form.getValue(dataPathKey), form.getValue(templatePathKey), form.getValue(outputDirKey));
                return "Slides have been successfully generated!";
            } catch (IOException e) {
                AppLogger.showError("Failed to generate slides!", e);
            }
            return null;
        });

        var versesKey = "verses";
        var versionsKey = "versions";
        var biblePPTGenerator = new Form("Bible Slides Generator", Map.of(
                versesKey, new FormInput("Verses", "text", "john 1:2-5,7-8"),
                versionsKey, new FormInput("Bible Versions", "text", "cuv,niv"),
                templatePathKey, new FormInput("Template File", "file", Config.getRelativePath("template-bible.pptx")),
                outputDirKey, outputDirDefault
        ), form -> {
            try {
                BibleSlidesGenerator.generate(form.getValue(templatePathKey), form.getValue(outputDirKey), form.getValue(versionsKey), form.getValue(versesKey));
                return "Bible slides have been successfully generated!";
            } catch (IOException e) {
                AppLogger.showError("Failed to generate slides!", e);
            }
            return null;
        });

        setLayout(new MigLayout());
        add(pptGenerator.getUI(), "wrap");
        add(biblePPTGenerator.getUI(), "wrap");
    }
}
