package com.scottmo.core.bible.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scottmo.core.appContext.impl.AppContextService;
import com.scottmo.core.bible.api.bibleOsis.Osis;
import com.scottmo.core.bible.impl.store.BibleStore;

@Component
public class BibleService {
    @Autowired
    private AppContextService appContextService;
    private BibleStore store;

    public BibleStore getStore() {
        if (store == null) {
            store = new BibleStore(Path.of(appContextService.getConfig().getDataDir()));
        }
        return store;
    }

    public void importOsisBible(File osisFile) throws IOException {
        String osisXML = Files.readString(osisFile.toPath(), StandardCharsets.UTF_8);
        Osis bibleOsis = Osis.of(osisXML);
        store.insert(bibleOsis.getVerses(), bibleOsis.getId());
    }
}
