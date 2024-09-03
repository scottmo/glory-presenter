package com.scottmo.api;

import com.scottmo.core.config.Config;
import com.scottmo.core.config.ConfigService;

public class AppConfigController {
    private ConfigService appContextService;

    Config getAppConfig() {
        return appContextService.getConfig();
    }
}
