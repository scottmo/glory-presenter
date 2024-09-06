package com.scottmo.ui.panels;

import javax.swing.JPanel;

public final class PPTXGeneratorsPanel extends JPanel {
    public PPTXGeneratorsPanel() {
        // common fields
        // var dataPathKey = "dataFilePath";
        // var templatePathKey = "tmplFilePath";
        // var outputDirKey = "outputDirPath";

        // common field defaults
        // var outputDirDefault = new FormInput(outputDirKey, "Output Folder", "directory", Config.getRelativePath("../output"));

        // var pptGenerator = new Form("PPTX Generator", List.of(
        //     new FormInput(dataPathKey, "Input File", "file", Config.getRelativePath(".")),
        //     new FormInput(templatePathKey, "Template File", "file", Config.getRelativePath(".")),
        //     outputDirDefault
        // ), form -> {
        //     try {
        //         PPTXGenerators.generate(form.getValue(dataPathKey), form.getValue(templatePathKey), form.getValue(outputDirKey));
        //         return "Slides have been successfully generated!";
        //     } catch (IOException e) {
        //         AppLogger.showError("Failed to generate slides!", e);
        //     }
        //     return null;
        // });

        // var versesKey = "verses";
        // var versionsKey = "versions";
        // var biblePPTGenerator = new Form("Bible Slides Generator", List.of(
        //     new FormInput(versesKey, "Verses", "text", "john 1:2-5,7-8"),
        //     new FormInput(versionsKey, "Bible Versions", "text", "cuv,niv"),
        //     new FormInput(templatePathKey, "Template File", "file", Config.getRelativePath("template-bible.pptx")),
        //     outputDirDefault
        // ), form -> {
        //     try {
        //         BibleSlidesGenerator.generate(form.getValue(templatePathKey), form.getValue(outputDirKey), form.getValue(versionsKey), form.getValue(versesKey));
        //         return "Bible slides have been successfully generated!";
        //     } catch (IOException e) {
        //         AppLogger.showError("Failed to generate slides!", e);
        //     }
        //     return null;
        // });

        // add(pptGenerator.getUI(), "wrap");
        // add(biblePPTGenerator.getUI(), "wrap");
    }
}
