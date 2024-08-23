package com.scottmo.core.appContext.api;

public interface AppContextService {

    AppConfig getConfig();

    void reload();

    String getRelativePath(String fileName);

    String getPPTXTemplate(String fileName);

    String getPrimaryLocale();

}
