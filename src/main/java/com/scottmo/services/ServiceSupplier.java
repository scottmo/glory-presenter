package com.scottmo.services;

import com.scottmo.config.AppContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ServiceSupplier {
    private static final AppContext appContext = new AppContext();
    public static AppContext getAppContext() {
        return appContext;
    }

    private static final Map<Class<?>, Object> services = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Service> Supplier<T> get(Class<T> clazz) {
        return () -> {
            T service;
            if (services.containsKey(clazz)) {
                return (T) services.get(clazz);
            }
            try {
                service = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Service needs to implement a default constructor", e);
            }
            services.put(clazz, service);
            return service;
        };
    }
}
