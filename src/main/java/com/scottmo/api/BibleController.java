package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.config.ConfigService;
import com.scottmo.core.ppt.api.BibleSlidesGenerator;
import com.scottmo.shared.StringUtils;

public class BibleController {

    private ConfigService appContextService;
    private BibleService bibleService;
    private BibleSlidesGenerator pptxGenerator;

    List<String> getVersions() {
        return bibleService.getAvailableVersions();
    }

    List<String> getBooks() {
        return new ArrayList<>(BibleMetadata.getBookInfoMap().keySet());
    }

    ResponseEntity<Map<String, Object>> importBibles(@RequestBody List<String> biblePaths) {
        if (biblePaths == null || biblePaths.isEmpty()) {
            return RequestUtil.errorResponse("No file to import!");
        }
        List<File> osisXMLFiles = biblePaths.stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : osisXMLFiles) {
            try {
                bibleService.importOsisBible(file);
            } catch (IOException e) {
                return RequestUtil.errorResponse("Failed to import bible [%s]!".formatted(file.getName()), e);
            }
        }
        return RequestUtil.successResponse();
    }

    public ResponseEntity<Resource> generatePPTX(
            @RequestParam String bibleRef,
            @RequestParam String templatePath) throws MalformedURLException, IOException {

        List<String> versions = new ArrayList<>(appContextService.getConfig().getBibleVersionToLocale().keySet());
        bibleRef = String.join(",", versions) + " - " + bibleRef;

        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(bibleRef) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = appContextService.getPPTXTemplate(templatePath);
        }
        pptxGenerator.generate(bibleRef, templatePath, outputPath.toString());
    
        return RequestUtil.download(outputPath);
    }
}
