package com.scottscmo.ppt;

import com.scottscmo.model.song.converters.KVMDConverter;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class PPTXGenerators {

    public static void generate(String dataFilePath, String tmplFilePath, String outputDirPath) throws IOException {
        String inputContent = Files.readString(Path.of(dataFilePath));
        var input = KVMDConverter.parse(inputContent);
        if (input == null) return;

        var sections = input.sections();
        if (sections == null || sections.isEmpty()) return;

        String title = input.title();
        String outputFilePath = Path.of(outputDirPath, title + ".pptx").toString();

        // Duplicate slide to match number of records.
        // Cannot modify here since the copies are using the same reference.
        // Modify after writing the copies.
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var ppt = new XMLSlideShow(inStream);
            var srcSlide = ppt.getSlides().get(0);
            for (int i = 0; i < sections.size(); i++) {
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
            for (int i = 0; i < sections.size(); i++) {
                var slide = ppt.getSlides().get(i);
                var section = sections.get(i);
                Map<String, String> values = section.text().entrySet().stream()
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
}
