package com.scottmo.services.bible;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scottmo.data.bibleOsis.Osis;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.bible.store.BibleStore;

@Component
public class BibleService {
    @Autowired
    private AppContextService appContextService;
    private BibleStore store;

    public BibleStore getStore() {
        if (store == null) {
            store = new BibleStore(Path.of(appContextService.getConfig().dataDir()));
        }
        return store;
    }

    public void importOsisBible(File osisFile) throws IOException {
        String osisXML = Files.readString(osisFile.toPath(), StandardCharsets.UTF_8);
        Osis bibleOsis = Osis.of(osisXML);
        store.insert(bibleOsis.getVerses(), bibleOsis.getId());
    }
}
