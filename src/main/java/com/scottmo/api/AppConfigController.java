package com.scottmo.api;

import com.scottmo.core.appContext.api.AppConfig;
import com.scottmo.core.appContext.api.AppContextService;

public class AppConfigController {
    private AppContextService appContextService;

    AppConfig getAppConfig() {
        return appContextService.getConfig();
    }
}
