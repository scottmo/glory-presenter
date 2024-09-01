package com.scottmo.api;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scottmo.core.google.api.GoogleCloudService;
import com.scottmo.core.google.api.SlideConfig;

public class GoogleController {
    private GoogleCloudService googleService;

    public ResponseEntity<Map<String, Object>> updateStyles(@PathVariable String id,
            @RequestBody SlideConfig slideConfig,
            @RequestParam Integer startIndex,
            @RequestParam Integer endIndex) {
        try {
            googleService.setBaseFont(id, slideConfig);
            return RequestUtil.successResponse();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return RequestUtil.errorResponse("Invalid slide config");
        } catch (IOException e) {
            e.printStackTrace();
            return RequestUtil.errorResponse("Failed to update styles of google slides");
        }
    }
}
