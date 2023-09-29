package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.data.bibleOsis.Osis;
import com.scottmo.services.bible.BibleService;

@RestController
@RequestMapping("/bible")
public class BibleController {

    @Autowired
    private BibleService bibleService;

    private RequestUtil requestUtil = new RequestUtil();

    @GetMapping("/versions")
    List<String> getVersions() {
        return bibleService.getStore().getAvailableVersions();
    }

    @GetMapping("/books")
    List<String> getBooks() {
        return new ArrayList<>(BibleMetadata.getBookInfoMap().keySet());
    }

    @PostMapping("/bible")
    ResponseEntity<Map<String, Object>> importBible(@RequestBody List<String> biblePaths) {
        if (biblePaths == null || biblePaths.isEmpty()) {
            return requestUtil.errorResponse("No file to import!");
        }
        List<File> osisXMLFiles = biblePaths.stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : osisXMLFiles) {
            try {
                String osisXML = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                Osis bibleOsis = Osis.of(osisXML);
                bibleService.getStore().insert(bibleOsis.getVerses(), bibleOsis.getId());
            } catch (IOException e) {
                return requestUtil.errorResponse("Failed to import bible [%s]!".formatted(file.getName()), e);
            }
        }
        return requestUtil.successResponse();
    }
}
