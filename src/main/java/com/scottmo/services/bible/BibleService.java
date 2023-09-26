package com.scottmo.services.bible;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.scottmo.services.appContext.AppContextService;

@Component("bibleService")
public class BibleService {
    @Autowired
    private AppContextService appContextService;
    private BibleStore store;

    public BibleStore getStore() {
        if (store == null) {
            store = new BibleStore(Path.of(appContextService.getConfig().dataDir()));
        }
        return store;
    }
}
