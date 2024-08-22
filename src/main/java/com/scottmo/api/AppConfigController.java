package com.scottmo.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scottmo.core.appContext.api.AppConfig;
import com.scottmo.core.appContext.impl.AppContextService;
import com.scottmo.core.google.impl.RequestUtil;

@RestController
@RequestMapping("/api/config")
public class AppConfigController {
    @Autowired
    private AppContextService appContextService;

    @GetMapping("")
    AppConfig getAppConfig() {
        return appContextService.getConfig();
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody AppConfig config) {
        // TODO
        return RequestUtil.successResponse();
    }
}
