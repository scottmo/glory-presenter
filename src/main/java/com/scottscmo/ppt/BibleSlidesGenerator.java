package com.scottscmo.ppt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.scottscmo.model.bible.BibleMetadata;
import com.scottscmo.model.bible.BibleMetadata.BookMetadata;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

public class BibleSlidesGenerator {
    private static final BibleTemplateHandler bibleTemplateHandler = new BibleTemplateHandler();

    static void insertBibleText(String templateFilePath, String outputFilePath, String bibleReference) throws IOException {
        try (FileInputStream inStream = new FileInputStream(templateFilePath)) {
            XMLSlideShow ppt = new XMLSlideShow(inStream);
            bibleTemplateHandler.insertBibleText(ppt, 1, bibleReference);
            ppt.removeSlide(0);

            // write new PPTX
            try (FileOutputStream outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    static void generateSlides(String templatePath, String destDir, String versions) throws IOException {
        Map<String, BookMetadata> bookInfoMap = BibleMetadata.getBookInfoMap();
        for (String bookId : bookInfoMap.keySet()) {
            BookMetadata booBookMetadata = bookInfoMap.get(bookId);
            for (int i = 0; i < booBookMetadata.count().length; i++) {
                String chapter = bookId + " " + (i + 1);
                insertBibleText(templatePath, destDir + "/" + chapter + ".pptx", versions + " - " + chapter);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String templatePath = "template-bible.pptx";
        generateSlides(templatePath, "bible_ppt", "cuv,niv");
    }
}
