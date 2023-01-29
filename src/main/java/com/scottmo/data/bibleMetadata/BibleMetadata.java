package com.scottmo.data.bibleMetadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BibleMetadata {
    private static final Map<String, BookMetadata> bookInfoMap;
    static {
        try (InputStream in = BibleMetadata.class.getClassLoader().getResourceAsStream("bibleMetadata.json")){
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, BookMetadata>> typeRef = new TypeReference<>() {};
            bookInfoMap = mapper.readValue(in, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bible metadata!", e);
        }
    }

    public static Map<String, BookMetadata> getBookInfoMap() {
        return bookInfoMap;
    }

    public static int getBookIndex(String id) {
        if (id != null && !id.isEmpty()) {
            BookMetadata bookMetadata = bookInfoMap.get(id);
            if (bookMetadata != null) {
                return bookMetadata.index();
            }
        }
        return -1;
    }

    public static String getBookId(int index) {
        return bookInfoMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().index() == index)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse("");
    }

    public static List<String> getBookIdsInOrder() {
        return bookInfoMap.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().index()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public static int getNumberOfBooks() {
        return bookInfoMap.size();
    }
}
