package com.scottscmo.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

public interface TemplateHandler {
    public void evaluateTemplate(XMLSlideShow xmlSlideShow, int index);

    default void replaceText(XSLFSlide slide, String searchText, String replacement) {
        for (XSLFTextShape textShape : slide.getPlaceholders()) {
            String text = textShape.getText();
            if (text.contains(searchText)) {
                text = text.replace(searchText, replacement);
                textShape.setText(text);
            }
        }
    }

    default String findText(XSLFSlide slide, String searchText) {
        for (XSLFTextShape textShape : slide.getPlaceholders()) {
            String text = textShape.getText();
            if (text.contains(searchText)) {
                return text;
            }
        }
        return null;
    }
}
