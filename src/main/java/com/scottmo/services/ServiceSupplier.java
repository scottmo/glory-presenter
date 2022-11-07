package com.scottmo.services;

import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.config.AppContext;
import com.scottmo.services.security.CipherService;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ServiceSupplier {
    private static final AppContext appContext = new AppContext();
    public static AppContext getAppContext() {
        return appContext;
    }

    private static BibleStore bibleStore;
    public static Supplier<BibleStore> getBibleStore() {
        return () -> {
            if (bibleStore == null) {
                bibleStore = new BibleStore(Path.of(appContext.getConfig().dataDir()));
            }
            return bibleStore;
        };
    }

    private static CipherService cipherService;
    public static Supplier<CipherService> getCipherService() {
        return () -> {
            if (cipherService == null) {
                cipherService = new CipherService();
            }
            return cipherService;
        };
    }
}
