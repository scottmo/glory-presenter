package com.scottmo.services.ppt;

import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.config.AppContext;
import com.scottmo.services.content.ContentUtil;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PPTXGenerators {

    private static final AppContext appContext = ServiceSupplier.getAppContext();

    public static void generate(String dataFilePath, String tmplFilePath, String outputDirPath) throws IOException {
        var content = ContentUtil.parse(Path.of(dataFilePath).toFile());
        if (content == null) return;

        var sections = content.getSections();
        if (sections == null || sections.isEmpty()) return;

        String title = content.getJoinedTitle(appContext.getConfig().googleSlideConfig().textConfigsOrder());
        String outputFilePath = Path.of(outputDirPath, title + ".pptx").toString();

        // Duplicate slide to match number of records.
        // Cannot modify here since the copies are using the same reference.
        // Modify after writing the copies.
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var ppt = new XMLSlideShow(inStream);
            var srcSlide = ppt.getSlides().get(0);
            for (int i = 0; i < content.getSectionOrder().size(); i++) {
                var slide = ppt.createSlide(srcSlide.getSlideLayout());
                slide.importContent(srcSlide);
            }
            ppt.removeSlide(0);
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }

        // Replace text for each slide.
        // Each slide's replacements corresponds to each item in data.
        try (var inStream = new FileInputStream(outputFilePath)) {
            var ppt = new XMLSlideShow(inStream);

            for (int i = 0; i < content.getSectionOrder().size(); i++) {
                var sectionName = content.getSectionOrder().get(i);
                var slide = ppt.getSlides().get(i);
                var section = sections.get(sectionName);
                Map<String, String> values = section.entrySet().stream()
                        .collect(Collectors.toMap(
                                s -> "{%s}".formatted(s.getKey()),
                                Map.Entry::getValue
                        ));
                values.put("{title}", title);
                TemplatingUtil.replaceText(slide, values);
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    public static void generate(String baseTemplate, List<InsertAction> insertActions, String outputPath) throws IOException {
        Path outputFile = Path.of(outputPath);
        XMLSlideShow outputPPT;

        if (baseTemplate != null) {
            Files.copy(Path.of(baseTemplate), outputFile, StandardCopyOption.REPLACE_EXISTING);
            try (var inStream = new FileInputStream(outputFile.toString())) {
                // this could be an issue. not sure if can close instream early
                outputPPT = new XMLSlideShow(inStream);
            }
        } else {
            outputPPT = new XMLSlideShow();
        }

        for (var insertAction : insertActions) {
            try (var inStream = new FileInputStream(insertAction.templatePath())) {
                var templatePPT = new XMLSlideShow(inStream);
                var templateSlides = templatePPT.getSlides();
                var content = ContentUtil.parse(new File(insertAction.dataPath()));
                var insertionIndex = Integer.parseInt(insertAction.insertIndex());

                for (var parameters : insertAction.parameters()) {
                    var templateSlide = templateSlides.get(parameters.templateIndex());
                }
            }
        }

        try (var outStream = new FileOutputStream(outputFile.toString())) {
            outputPPT.write(outStream);
        }
        outputPPT.close();
    }

    public static void main(String[] args) {
        try (var inStream = new FileInputStream(appContext.getRelativePath("templates/template-hymn.pptx"))) {
            var ppt = new XMLSlideShow(inStream);
            var outputPPT = new XMLSlideShow();
            var slide = outputPPT.createSlide();
//            slide.importContent(ppt.getSlides().get(0));
            TemplatingUtil.replaceText(slide, Map.of(
                    "{metadata.index}", "321",
                    "{title.zh}", "你好",
                    "{title.en}", "hello"
            ));
            try (var outStream = new FileOutputStream(appContext.getRelativePath("test.pptx"))) {
                outputPPT.write(outStream);
            }
            outputPPT.close();
            ppt.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
