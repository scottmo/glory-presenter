package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.bible.BibleService;
import com.scottmo.services.ppt.BibleSlidesGenerator;
import com.scottmo.util.StringUtils;


@RestController
@RequestMapping("/api/bible")
public class BibleController {

    @Autowired
    private AppContextService appContextService;
    @Autowired
    private BibleService bibleService;
    @Autowired
    private BibleSlidesGenerator pptxGenerator;

    private RequestUtil requestUtil = new RequestUtil();

    @GetMapping("/versions")
    List<String> getVersions() {
        return bibleService.getStore().getAvailableVersions();
    }

    @GetMapping("/books")
    List<String> getBooks() {
        return new ArrayList<>(BibleMetadata.getBookInfoMap().keySet());
    }

    @PostMapping("/import")
    ResponseEntity<Map<String, Object>> importBibles(@RequestBody List<String> biblePaths) {
        if (biblePaths == null || biblePaths.isEmpty()) {
            return requestUtil.errorResponse("No file to import!");
        }
        List<File> osisXMLFiles = biblePaths.stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : osisXMLFiles) {
            try {
                bibleService.importOsisBible(file);
            } catch (IOException e) {
                return requestUtil.errorResponse("Failed to import bible [%s]!".formatted(file.getName()), e);
            }
        }
        return requestUtil.successResponse();
    }

    @GetMapping("/pptx")
    public ResponseEntity<Resource> generatePPTX(
            @RequestParam String bibleRef,
            @RequestParam String templatePath) throws MalformedURLException, IOException {

        List<String> versions = new ArrayList<>(appContextService.getConfig().bibleVersionToLocale().keySet());
        bibleRef = String.join(",", versions) + " - " + bibleRef;

        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(bibleRef) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = appContextService.getPPTXTemplate(templatePath);
        }
        pptxGenerator.generate(bibleRef, templatePath, outputPath.toString());
    
        return requestUtil.download(outputPath);
    }
}
