package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.config.AppContext;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.logging.AppLoggerService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;

import java.util.function.Supplier;

public class BibleTabController {
    private final AppContext appContext = ServiceSupplier.getAppContext();
    private final Supplier<AppLoggerService> logger = ServiceSupplier.get(AppLoggerService.class);

    public TextField templatePathInput;
    public TextField bibleReferenceInput;

    public void initialize() {
        Platform.runLater(() -> {
            ViewUtil.get().attachFilePickerToInput(templatePathInput, appContext.getPPTXTemplate(""), ViewUtil.FILE_EXT_PPTX);
        });
    }

    public void onImport(ActionEvent actionEvent) {
    }

    public void onGeneratePPTX(ActionEvent actionEvent) {
    }
}
