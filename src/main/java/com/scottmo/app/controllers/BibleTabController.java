package com.scottmo.app.controllers;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.config.AppContext;
import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.data.bibleOsis.Osis;
import com.scottmo.services.ServiceSupplier;
import com.scottmo.services.bible.BibleStore;
import com.scottmo.services.logging.AppLoggerService;
import com.scottmo.services.ppt.BibleSlidesGenerator;
import com.scottmo.util.StringUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BibleTabController {
    private final AppContext appContext = ServiceSupplier.getAppContext();
    private final Supplier<BibleStore> bibleStore = ServiceSupplier.get(BibleStore.class);
    private final Supplier<AppLoggerService> logger = ServiceSupplier.get(AppLoggerService.class);

    public TextField templatePathInput;
    public TextField bibleReferenceInput;
    public MenuButton bibleBookPicker;
    public Label availableVersionsText;
    public CheckBox hasStartSlide;
    public CheckBox hasEndSlide;

    public void initialize() {
        Platform.runLater(() -> {
            refreshAvailableVersionsList();
            BibleMetadata.getBookInfoMap().keySet().forEach(verseName -> {
                MenuItem item = new MenuItem(verseName);
                item.setOnAction(this::onAddBook);
                bibleBookPicker.getItems().add(item);
            });
            ViewUtil.get().attachFilePickerToInput(templatePathInput, appContext.getPPTXTemplate(""), ViewUtil.FILE_EXT_PPTX);
        });
    }

    public void onImport(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(ViewUtil.FILE_EXT_BIBLEOSIS);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getStage());
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                try {
                    String osisXML = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    Osis bibleOsis = Osis.of(osisXML);
                    bibleStore.get().insert(bibleOsis.getVerses(), bibleOsis.getId());
                } catch (IOException e) {
                    logger.get().error("Failed to import bible [%s]!".formatted(file.getName()), e);
                }
            }
            refreshAvailableVersionsList();
            logger.get().info("Done importing bibles!");
        }
    }

    public void onGeneratePPTX(ActionEvent actionEvent) {
        List<String> versions = new ArrayList<>(appContext.getConfig().bibleVersionToLocale().keySet());
        String bibleRef =String.join(",", versions)+ " - " + bibleReferenceInput.getText();

        String outputFilePath = appContext.getOutputDir(StringUtils.sanitizeFilename(bibleRef) + ".pptx");
        String templateFilePath = templatePathInput.getText();
        if (!templateFilePath.contains("/")) {
            templateFilePath = appContext.getPPTXTemplate(templateFilePath);
        }
        try {
            BibleSlidesGenerator.generate(bibleRef, templateFilePath, outputFilePath,
                    hasStartSlide.isSelected(), hasEndSlide.isSelected());
            logger.get().info("Generated slides at " + outputFilePath);
        } catch (IOException e) {
            logger.get().error("Failed to generate slides!", e);
        }
    }

    private void onAddBook(ActionEvent event) {
        String bookName = ((MenuItem)event.getSource()).getText();
        bibleReferenceInput.setText(bookName);
    }

    private void refreshAvailableVersionsList() {
        availableVersionsText.setText(String.join(", ", bibleStore.get().getAvailableVersions()));
    }

    private Stage getStage() {
        return ViewUtil.get().getStage(templatePathInput);
    }
}
