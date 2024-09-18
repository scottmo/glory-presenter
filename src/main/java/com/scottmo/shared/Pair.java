package com.scottmo.shared;

import java.util.ArrayList;
import java.util.List;

public record Pair<K, V>(K key, V value) {
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @SafeVarargs
    public static <K, V> List<Pair<K, V>> ofList(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("There must be an even number of arguments (key-value pairs).");
        }

        List<Pair<K, V>> pairs = new ArrayList<>();

        for (int i = 0; i < keyValues.length; i += 2) {
            K key = (K) keyValues[i];
            V value = (V) keyValues[i + 1];
            pairs.add(new Pair<>(key, value));
        }

        return pairs;
    }
}
