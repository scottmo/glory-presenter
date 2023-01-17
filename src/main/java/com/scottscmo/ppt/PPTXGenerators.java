package com.scottscmo.ppt;

import com.google.common.base.Strings;
import com.scottscmo.Config;
import com.scottscmo.model.content.ContentUtil;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PPTXGenerators {

    public static void generate(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath) throws IOException {
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var tmplSlides = new XMLSlideShow(inStream);
            // make copies of template slides
            var numPlaceholderSlides = tmplSlides.getSlides().size();
            int currentTemplateIndex = 0;
            var srcSlide = tmplSlides.getSlides().get(currentTemplateIndex);
            for (int j = 0; j < contents.size(); j++) {
                var slide = tmplSlides.createSlide(srcSlide.getSlideLayout());
                slide.importContent(srcSlide);
            }
            // remove template slides
            for (int i = 0; i < numPlaceholderSlides; i++) {
                tmplSlides.removeSlide(0);
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                tmplSlides.write(outStream);
            }
            tmplSlides.close();
        }

        try (var inStream = new FileInputStream(outputFilePath)) {
            var ppt = new XMLSlideShow(inStream);
            for (int i = 0; i < contents.size(); i++) {
                var slide = ppt.getSlides().get(i);
                Map<String, String> values = contents.get(i);
                TemplatingUtil.replaceText(slide, values);
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    public static void generate(String dataFilePath, String tmplFilePath, String outputDirPath) throws IOException {
        var content = ContentUtil.parse(Path.of(dataFilePath).toFile());
        if (content == null) return;

        var sections = content.getSections();
        if (sections == null || sections.isEmpty()) return;

        String title = content.getJoinedTitle(Config.get().googleSlideConfig().textConfigsOrder());
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

    public static void main(String[] args) throws IOException {
        String templatePath = Config.getRelativePath("templates/test-template.pptx");
        String outputPath = Config.getRelativePath("test.pptx");
        List<Map<String, String>> values = List.of(
                Map.of(
                        "{title}", "321",
                        "{zh}", "你好",
                        "{en}", "hello"
                ),
                Map.of(
                        "{title}", "123",
                        "{zh}", "你好巴拉巴拉",
                        "{en}", "hello blala"
                )
        );
        generate(values, templatePath, outputPath);
    }
}
