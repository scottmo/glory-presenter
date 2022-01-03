package com.scottscmo.ppt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

public class Presentation {
    private static final BibleTemplateHandler bibleTemplateHandler = new BibleTemplateHandler();
    private static final SongTemplateHandler songTemplateHandler = new SongTemplateHandler();

    public static void createFromTemplate(String templateFilePath, String outputFilePath) throws IOException {
        try (FileInputStream inStream = new FileInputStream(new File(templateFilePath))) {
            XMLSlideShow ppt = new XMLSlideShow(inStream);
            List<XSLFSlide> slides = ppt.getSlides();

            for (int i = 0; i < slides.size(); i++) {
                bibleTemplateHandler.evaluateTemplate(ppt, slides.get(i).getSlideNumber() - 1);
                songTemplateHandler.evaluateTemplate(ppt, slides.get(i).getSlideNumber() - 1);
            }

            // write new PPTX
            try (FileOutputStream outStream = new FileOutputStream(new File(outputFilePath))) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    public static void insertBibleText(String templateFilePath, String outputFilePath, String bibleReference) throws IOException {
        try (FileInputStream inStream = new FileInputStream(new File(templateFilePath))) {
            XMLSlideShow ppt = new XMLSlideShow(inStream);
            bibleTemplateHandler.insertBibleText(ppt, 1, bibleReference);

            // write new PPTX
            try (FileOutputStream outStream = new FileOutputStream(new File(outputFilePath))) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    public static void main(String[] args) throws IOException {
        insertBibleText("template.pptx", "psalms 1.pptx", "cuv,niv - psalms 1");
    }
}
