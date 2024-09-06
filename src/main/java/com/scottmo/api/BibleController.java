package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ServiceProvider;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.ppt.api.BibleSlidesGenerator;
import com.scottmo.shared.StringUtils;

public class BibleController {

    private ConfigService configService = ConfigService.get();
    private BibleService bibleService = ServiceProvider.get(BibleService.class).get();
    private BibleSlidesGenerator pptxGenerator = ServiceProvider.get(BibleSlidesGenerator.class).get();

    public List<String> getVersions() {
        return bibleService.getAvailableVersions();
    }

    public List<String> getBooks() {
        return new ArrayList<>(BibleMetadata.getBookInfoMap().keySet());
    }

    public boolean importBibles(List<String> biblePaths) {
        if (biblePaths == null || biblePaths.isEmpty()) {
            throw new IllegalArgumentException("No file to import!");
        }
        List<File> osisXMLFiles = biblePaths.stream()
                .map(path -> new File(path))
                .collect(Collectors.toList());
        for (File file : osisXMLFiles) {
            try {
                bibleService.importOsisBible(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to import bible [%s]!".formatted(file.getName()), e);
            }
        }
        return true;
    }

    public boolean generatePPTX(String bibleRef, String templatePath) throws MalformedURLException, IOException {

        List<String> versions = new ArrayList<>(configService.getConfig().getBibleVersionToLocale().keySet());
        bibleRef = String.join(",", versions) + " - " + bibleRef;

        String outputPath = configService.getOutputPath(StringUtils.sanitizeFilename(bibleRef) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = configService.getPPTXTemplate(templatePath);
        }
        pptxGenerator.generate(bibleRef, templatePath, outputPath.toString());

        return true;
    }
}
