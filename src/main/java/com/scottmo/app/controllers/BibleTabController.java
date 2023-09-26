package com.scottmo.app.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.scottmo.app.views.ViewUtil;
import com.scottmo.data.bibleMetadata.BibleMetadata;
import com.scottmo.data.bibleOsis.Osis;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.bible.BibleService;
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

public class BibleTabController {
    @Autowired
    private AppContextService appContextService;
    @Autowired
    private BibleService bibleService;
    @Autowired
    private BibleSlidesGenerator pptxGenerator;
    @Autowired
    private AppLoggerService logger;

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
            ViewUtil.get().attachFilePickerToInput(templatePathInput, appContextService.getPPTXTemplate(""), ViewUtil.FILE_EXT_PPTX);
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
                    bibleService.getStore().insert(bibleOsis.getVerses(), bibleOsis.getId());
                } catch (IOException e) {
                    logger.error("Failed to import bible [%s]!".formatted(file.getName()), e);
                }
            }
            refreshAvailableVersionsList();
            logger.info("Done importing bibles!");
        }
    }

    public void onGeneratePPTX(ActionEvent actionEvent) {
        List<String> versions = new ArrayList<>(appContextService.getConfig().bibleVersionToLocale().keySet());
        String bibleRef =String.join(",", versions)+ " - " + bibleReferenceInput.getText();

        String outputFilePath = appContextService.getOutputDir(StringUtils.sanitizeFilename(bibleRef) + ".pptx");
        String templateFilePath = templatePathInput.getText();
        if (!templateFilePath.contains("/")) {
            templateFilePath = appContextService.getPPTXTemplate(templateFilePath);
        }
        try {
            pptxGenerator.generate(bibleRef, templateFilePath, outputFilePath,
                    hasStartSlide.isSelected(), hasEndSlide.isSelected());
            logger.info("Generated slides at " + outputFilePath);
        } catch (IOException e) {
            logger.error("Failed to generate slides!", e);
        }
    }

    private void onAddBook(ActionEvent event) {
        String bookName = ((MenuItem)event.getSource()).getText();
        bibleReferenceInput.setText(bookName);
    }

    private void refreshAvailableVersionsList() {
        availableVersionsText.setText(String.join(", ", bibleService.getStore().getAvailableVersions()));
    }

    private Stage getStage() {
        return ViewUtil.get().getStage(templatePathInput);
    }
}
