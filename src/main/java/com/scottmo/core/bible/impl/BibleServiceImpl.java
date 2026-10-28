package com.scottmo.core.bible.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.scottmo.config.ConfigService;
import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.api.bibleMetadata.BibleMetadata;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleOsis.Osis;
import com.scottmo.core.bible.api.bibleReference.BibleReference;
import com.scottmo.core.bible.impl.store.BibleStore;

public class BibleServiceImpl implements BibleService, AutoCloseable {
    private final Supplier<Path> storeLocation;
    private volatile BibleStore store;

    public BibleServiceImpl() {
        this(() -> Path.of(ConfigService.get().getConfig().getDataDir()));
    }

    /** Creates a service backed by an explicit data directory, useful for isolated callers and tests. */
    public BibleServiceImpl(Path dataDirectory) {
        this(() -> dataDirectory);
    }

    private BibleServiceImpl(Supplier<Path> storeLocation) {
        this.storeLocation = Objects.requireNonNull(storeLocation, "storeLocation");
    }

    private synchronized BibleStore getStore() {
        if (store == null) {
            store = new BibleStore(storeLocation.get());
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
    public List<String> getBooks() {
        return new ArrayList<>(BibleMetadata.getBookInfoMap().keySet());
    }

    @Override
    public Map<String, String> getBookNames(String bookId) {
        return getStore().getBookNames(bookId);
    }

    @Override
    public void importBible(String osisFilePath) throws IOException {
        String osisXML = Files.readString(Path.of(osisFilePath), StandardCharsets.UTF_8);
        Osis bibleOsis = Osis.of(osisXML);
        getStore().insert(bibleOsis.getVerses(), bibleOsis.getId());
    }

    @Override
    public synchronized void close() throws SQLException {
        if (store != null) {
            store.close();
            store = null;
        }
    }

    @Override
    public List<String> importBibles(List<String> oasisFilePaths) {
        List<String> failedImports = new ArrayList<>();
        if (oasisFilePaths == null || oasisFilePaths.isEmpty()) {
            throw new IllegalArgumentException("No file to import!");
        }
        oasisFilePaths.stream().forEach(filePath -> {
            try {
                importBible(filePath);
            } catch (IOException e) {
                failedImports.add(filePath);
                e.printStackTrace();
            }
        });
        return failedImports;
    }
}
