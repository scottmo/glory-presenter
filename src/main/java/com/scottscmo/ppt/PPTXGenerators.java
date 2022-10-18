package com.scottscmo.ppt;

import com.scottscmo.Config;
import com.scottscmo.model.content.ContentUtil;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class PPTXGenerators {

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
}
