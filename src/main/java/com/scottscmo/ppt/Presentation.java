package com.scottscmo.ppt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

public class Presentation {
    public static void test() throws IOException {
        // create a new PPTX file
        FileOutputStream fileOutputStream = new FileOutputStream(new File("Slidelayout.pptx"));
        // create a new slide show
        XMLSlideShow xmlSlideShow = new XMLSlideShow();
        // initialize slide master object
        XSLFSlideMaster xslfSlideMaster = xmlSlideShow.getSlideMasters().get(0);
        // set Title layout
        XSLFSlideLayout xslfSlideLayout = xslfSlideMaster.getLayout(SlideLayout.TITLE);
        // create a new slide with title layout
        XSLFSlide xslfSlide = xmlSlideShow.createSlide(xslfSlideLayout);
        // select place holder
        XSLFTextShape xslfTextShape = xslfSlide.getPlaceholder(0);
        // set title
        xslfTextShape.setText("Test");
        // save file
        xmlSlideShow.write(fileOutputStream);
        // close stream
        fileOutputStream.close();
    }
}
