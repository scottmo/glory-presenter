package com.scottmo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scottmo.services.bible.BibleService;

@RestController
public class BibleController {

    @Autowired
    private BibleService bibleService;

    @GetMapping("/bible-versions")
    List<String> bibleVersions() {
        return bibleService.getStore().getAvailableVersions();
    }
}
