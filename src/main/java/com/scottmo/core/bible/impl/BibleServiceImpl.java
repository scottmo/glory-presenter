package com.scottmo.core.bible.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.scottmo.core.appContext.api.AppContextService;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleOsis.Osis;
import com.scottmo.core.bible.api.bibleReference.BibleReference;
import com.scottmo.core.bible.impl.store.BibleStore;

public class BibleServiceImpl implements BibleService {
    private AppContextService appContextService;
    private BibleStore store;

    private BibleStore getStore() {
        if (store == null) {
            store = new BibleStore(Path.of(appContextService.getConfig().getDataDir()));
        }
        return store;
    }

    @Override
    public int insert(Map<String, List<List<String>>> bible, String version) {
        return getStore().insert(bible, version);
    }

    @Override
    public Map<String, List<BibleVerse>> getBibleVerses(BibleReference ref) {
        return getStore().getBibleVerses(ref);
    }

    @Override
    public List<String> getAvailableVersions() {
        return getStore().getAvailableVersions();
    }

    @Override
    public Map<String, String> getBookNames(String bookId) {
        return getStore().getBookNames(bookId);
    }

    @Override
    public void importOsisBible(File osisFile) throws IOException {
        String osisXML = Files.readString(osisFile.toPath(), StandardCharsets.UTF_8);
        Osis bibleOsis = Osis.of(osisXML);
        store.insert(bibleOsis.getVerses(), bibleOsis.getId());
    }
}
