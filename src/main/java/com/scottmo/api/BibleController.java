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
        bibleService.importBibles(biblePaths);
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
