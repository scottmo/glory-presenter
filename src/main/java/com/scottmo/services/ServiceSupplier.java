package com.scottmo.services;

import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.config.AppConfigProvider;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ServiceSupplier {
    private static BibleStore bibleStore;
    public static Supplier<BibleStore> getBibleStore() {
        return () -> {
            if (bibleStore == null) {
                bibleStore = new BibleStore(Path.of(AppConfigProvider.get().dataDir()));
            }
            return bibleStore;
        };
    }
}
