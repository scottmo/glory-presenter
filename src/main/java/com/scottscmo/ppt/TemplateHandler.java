package com.scottscmo.ppt;

import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import java.util.List;
import java.util.Map;

public interface TemplateHandler {
    void evaluateTemplate(XMLSlideShow ppt, int index);

    default void replaceText(XSLFSlide slide, Map<String, String> replacements) {
        for (XSLFTextShape textShape : slide.getPlaceholders()) {
            String text = textShape.getText();
            for (String searchText : replacements.keySet()) {
                if (text.contains(searchText)) {
                    text = text.replace(searchText, replacements.get(searchText));
                }
            }
            clearText(textShape);
            appendText(textShape, text);
        }
    }

    default void appendText(XSLFTextShape textShape, String text) {
        String[] lines = text.trim().split("\n");
        List<XSLFTextParagraph> pps = textShape.getTextParagraphs();
        XSLFTextParagraph pp = pps.get(pps.size() - 1);

        for (int i = 0; i < lines.length; i++) {
            pp.addNewTextRun().setText(lines[i]);
            if (i + 1 < lines.length) {
                pp.addLineBreak();
            }
        }
    }

    /**
     * Helper to remove all texts and new lines.
     * XSLFTextShape.setText("") is bugged when there's new line
     */
    default void clearText(XSLFTextShape textShape) {
        for (XSLFTextParagraph pp : textShape.getTextParagraphs()) {
            for (XSLFTextRun textRun : pp.getTextRuns()) {
                if (!textRun.getRawText().equals("\n")) {
                    textRun.setText("");
                }
            }
            CTTextParagraph ppxml = pp.getXmlObject();
            for (int i = ppxml.sizeOfBrArray(); i>0; i--) {
                ppxml.removeBr(i-1);
            }
        }
    }

    default void replaceText(XSLFSlide slide, String searchText, String replacement) {
        replaceText(slide, Map.of(searchText, replacement));
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

    default XSLFSlideMaster getSlideMaster(XMLSlideShow ppt, String name) {
        if (ppt == null || name == null || name.isEmpty()) return null;

        return ppt.getSlideMasters().stream()
                .filter(master -> master.getTheme().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
