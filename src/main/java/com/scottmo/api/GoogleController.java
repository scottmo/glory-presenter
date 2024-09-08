package com.scottmo.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scottmo.core.google.api.GoogleCloudService;
import com.scottmo.core.google.api.SlideConfig;

public class GoogleController {
    private GoogleCloudService googleService;

    public boolean updateStyles(String id, SlideConfig slideConfig, Integer startIndex, Integer endIndex) {
        try {
            googleService.setBaseFont(id, slideConfig);
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid slide config", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update styles of google slides", e);
        }
    }
}
