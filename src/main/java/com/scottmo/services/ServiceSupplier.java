package com.scottmo.services;

import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.config.AppContext;
import com.scottmo.services.security.CipherService;
import com.scottmo.services.songs.SongStore;
import com.scottmo.services.songsOpenLyrics.SongsOpenLyricsService;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ServiceSupplier {
    private static final AppContext appContext = new AppContext();
    public static AppContext getAppContext() {
        return appContext;
    }

    private static final Map<Class<?>, Object> services = new HashMap<>();

    public static <T extends Service> Supplier<T> get(Class<T> clazz) {
        return () -> {
            Service service = null;
            if (services.containsKey(clazz)) {
                return (T) services.get(clazz);
            }

            if (clazz == BibleStore.class) {
                service = new BibleStore(Path.of(appContext.getConfig().dataDir()));
            }
            if (clazz == SongStore.class) {
                service = new SongStore(Path.of(appContext.getConfig().dataDir()));
            }
            if (clazz == CipherService.class) {
                service = new CipherService();
            }
            if (clazz == SongsOpenLyricsService.class) {
                service = new SongsOpenLyricsService();
            }
            if (service != null) {
                services.put(clazz, service);
                return (T) service;
            }
            return null;
        };
    }
}
