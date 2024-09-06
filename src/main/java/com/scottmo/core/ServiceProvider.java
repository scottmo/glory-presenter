package com.scottmo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JFrame;

import com.scottmo.core.bible.api.BibleService;
import com.scottmo.core.bible.impl.BibleServiceImpl;
import com.scottmo.core.google.api.GoogleCloudService;
import com.scottmo.core.google.impl.GoogleCloudServiceImpl;
import com.scottmo.core.ppt.api.BibleSlidesGenerator;
import com.scottmo.core.ppt.api.SongSlidesGenerator;
import com.scottmo.core.ppt.impl.BibleSlidesGeneratorImpl;
import com.scottmo.core.ppt.impl.SongSlidesGeneratorImpl;
import com.scottmo.core.security.api.CipherService;
import com.scottmo.core.security.impl.CipherServiceImpl;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.impl.SongServiceImpl;

public class ServiceProvider {
    private static final Map<Class<?>, Object> services = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Service> Supplier<T> get(Class<T> clazz) {
        return () -> {
            T service;
            if (services.containsKey(clazz)) {
                return (T) services.get(clazz);
            }
            service = getRaw(clazz);
            if (service != null) {
                services.put(clazz, service);
            }
            return service;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Service> T getRaw(Class<T> clazz) {
        if (SongService.class.isAssignableFrom(clazz)) {
            return (T) new SongServiceImpl();
        }
        if (GoogleCloudService.class.isAssignableFrom(clazz)) {
            return (T) new GoogleCloudServiceImpl();
        }
        if (BibleService.class.isAssignableFrom(clazz)) {
            return (T) new BibleServiceImpl();
        }
        if (CipherService.class.isAssignableFrom(clazz)) {
            return (T) new CipherServiceImpl();
        }
        if (BibleSlidesGenerator.class.isAssignableFrom(clazz)) {
            return (T) new BibleSlidesGeneratorImpl();
        }
        if (SongSlidesGenerator.class.isAssignableFrom(clazz)) {
            return (T) new SongSlidesGeneratorImpl();
        }
        return null;
    }
}
