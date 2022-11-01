package com.scottmo.services.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class TemplatingUtil {

    public static String getText(XSLFSlide slide) {
        return slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .map(s -> ((XSLFTextShape)s).getTextParagraphs().stream()
                    .map(XSLFTextParagraph::getText)
                    .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }

    public static void replaceText(XSLFSlide slide, Map<String, String> replacements) {
        slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .forEach(s -> replaceText((XSLFTextShape) s, replacements));
    }

    private static void replaceText(XSLFTextShape shape, Map<String, String> replacements) {
        for (var pp : shape.getTextParagraphs()) {
            String text = pp.getText();
            clearText(pp);
            for (var entry : replacements.entrySet()) {
                if (text.contains(entry.getKey())) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
            setText(pp, text);
        }
    }

    public static void appendText(XSLFTextShape textShape, String text) {
        String[] lines = text.trim().split("\n");
        var pps = textShape.getTextParagraphs();
        var pp = pps.get(pps.size() - 1);
        for (int i = 0; i < lines.length; i++) {
            pp.addNewTextRun().setText(lines[i]);
            if (i + 1 < lines.length) {
                pp.addLineBreak();
            }
        }
    }

    /**
     * setText that handles "\n" properly
     */
    public static void setText(XSLFTextParagraph pp, String text) {
        var baseTextRun = pp.getTextRuns().isEmpty()
                ? pp.addNewTextRun()
                : pp.getTextRuns().get(0);
        String[] lines = text.split("\n");
        baseTextRun.setText(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            pp.addLineBreak();
            var newTextRun = pp.addNewTextRun();
            newTextRun.setText(lines[i]);
            newTextRun.setFontColor(baseTextRun.getFontColor());
            newTextRun.setFontFamily(baseTextRun.getFontFamily());
            newTextRun.setFontSize(baseTextRun.getFontSize());
        }
    }

    /**
     * Helper to remove all texts and new lines.
     * XSLFTextShape.setText("") is bugged when there's new line
     */
    public static void clearText(XSLFTextShape textShape) {
        textShape.getTextParagraphs().stream()
                .forEach(TemplatingUtil::clearText);
    }

    public static void clearText(XSLFTextParagraph pp) {
        for (var textRun : pp.getTextRuns()) {
            if (!textRun.getRawText().equals("\n")) {
                textRun.setText("");
            }
        }
        var ppxml = pp.getXmlObject();
        if (ppxml.sizeOfBrArray() > 0) {
            for (int i = ppxml.sizeOfBrArray() - 1; i >= 0; i--) {
                ppxml.removeBr(i);
            }
        }
    }

    public static void replacePlaceholders(XSLFSlide slide, Map<String, String> replacements) {
        for (var textShape : slide.getPlaceholders()) {
            String text = textShape.getText();
            for (var entry : replacements.entrySet()) {
                if (text.contains(entry.getKey())) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
            clearText(textShape);
            appendText(textShape, text);
        }
    }

    public static void replacePlaceholders(XSLFSlide slide, String searchText, String replacement) {
        replacePlaceholders(slide, Map.of(searchText, replacement));
    }

    public static String findPlaceholderText(XSLFSlide slide, String searchText) {
        for (var placeholder : slide.getPlaceholders()) {
            String text = placeholder.getText();
            if (text.contains(searchText)) {
                return text;
            }
        }
        return null;
    }

    public static XSLFSlideMaster getSlideMaster(XMLSlideShow ppt, String name) {
        return ppt.getSlideMasters().stream()
                .filter(master -> name.equals(master.getTheme().getName()))
                .findFirst()
                .orElse(null);
    }

    public static XSLFSlideLayout getSlideMasterLayout(XMLSlideShow ppt, String name, String layout) {
        return Optional.ofNullable(getSlideMaster(ppt, name))
                .map(master -> master.getLayout(layout))
                .orElse(null);
    }
}
