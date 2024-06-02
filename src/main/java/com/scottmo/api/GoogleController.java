package com.scottmo.api;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.google.GoogleCloudService;
import com.scottmo.services.google.SlideConfig;

@RestController
@RequestMapping("/api/google")
public class GoogleController {
    @Autowired
    private AppContextService appContextService;
    @Autowired
    private GoogleCloudService googleService;

    @GetMapping("/updatestyles")
    public ResponseEntity<Map<String, Object>> updateStyles(@PathVariable String pptId,
            @RequestParam String slideConfigString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SlideConfig slideConfig = objectMapper.readValue(slideConfigString, SlideConfig.class);
            googleService.setDefaultTitleText(pptId, slideConfig);
            return RequestUtil.successResponse();
        } catch (JsonProcessingException e) {
            return RequestUtil.errorResponse("Invalid slide config");
        } catch (IOException e) {
            return  RequestUtil.errorResponse("Failed to update styles of google slides");
        }
    }
}
