package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.config.AppContext;
import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.logging.AppLoggerService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.function.Supplier;

public class BibleTabController {
    private final AppContext appContext = ServiceSupplier.getAppContext();
    private final Supplier<BibleStore> bibleStore = ServiceSupplier.get(BibleStore.class);
    private final Supplier<AppLoggerService> logger = ServiceSupplier.get(AppLoggerService.class);

    public TextField templatePathInput;
    public TextField bibleReferenceInput;
    public MenuButton bibleBookPicker;
    public Label availableVersionsText;

    public void initialize() {
        Platform.runLater(() -> {
            availableVersionsText.setText(String.join(", ", bibleStore.get().getAvailableVersions()));
            BibleMetadata.getBookInfoMap().keySet().forEach(verseName -> {
                MenuItem item = new MenuItem(verseName);
                item.setOnAction(this::onAddBook);
                bibleBookPicker.getItems().add(item);
            });
            ViewUtil.get().attachFilePickerToInput(templatePathInput, appContext.getPPTXTemplate(""), ViewUtil.FILE_EXT_PPTX);
        });
    }

    public void onImport(ActionEvent actionEvent) {
    }

    public void onGeneratePPTX(ActionEvent actionEvent) {
    }

    private void onAddBook(ActionEvent event) {
        String bookName = ((MenuItem)event.getSource()).getText();
        bibleReferenceInput.setText(bookName);
    }
}
