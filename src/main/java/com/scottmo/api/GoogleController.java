package com.scottmo.api;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String, Object>> updateStyles(String pptId, SlideConfig slideConfig) {
        try {
            googleService.setDefaultTitleText(pptId, slideConfig);
            return RequestUtil.successResponse();
        } catch (IOException e) {
            return  RequestUtil.errorResponse("Failed to update styles of google slides");
        }
    }
}
