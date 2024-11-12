package com.scottmo.core.ppt.impl;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class TemplatingUtil {
    static final String PLACEHOLDER_TEMPLATE = "{%s}";

    static String getText(XSLFSlide slide) {
        return slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .map(s -> ((XSLFTextShape)s).getTextParagraphs().stream()
                    .map(XSLFTextParagraph::getText)
                    .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }

    static void removeAllTexts(XSLFSlide slide, String regex) {
        slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .forEach(s -> {
                    XSLFTextShape shape = (XSLFTextShape) s;
                    for (var pp : shape.getTextParagraphs()) {
                        String text = pp.getText();
                        clearText(pp);
                        text = text.replaceAll(regex, "");
                        setText(pp, text);
                    }
                });
    }

    static void replaceText(XSLFSlide slide, Map<String, String> replacements) {
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

    private static void resetSpacing(XSLFTextParagraph pp) {
        pp.setSpaceBefore(0.0);
        pp.setSpaceAfter(0.0);
        pp.setIndent(0.0);
        pp.setLineSpacing(100.0);
    }

    static void appendText(XSLFTextShape textShape, String text) {
        String[] lines = text.trim().split("\n");
        var pps = textShape.getTextParagraphs();
        var pp = pps.get(pps.size() - 1);
        for (int i = 0; i < lines.length; i++) {
            pp.addNewTextRun().setText(lines[i]);
            if (i + 1 < lines.length) {
                pp.addLineBreak();
            }
        }
        resetSpacing(pp);
    }

    /**
     * setText that handles "\n" properly
     */
    static void setText(XSLFTextParagraph pp, String text) {
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
        resetSpacing(pp);
    }

    /**
     * Helper to remove all texts and new lines.
     * XSLFTextShape.setText("") is bugged when there's new line
     */
    static void clearText(XSLFTextShape textShape) {
        textShape.getTextParagraphs().forEach(TemplatingUtil::clearText);
    }

    static void clearText(XSLFTextParagraph pp) {
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

    static void replacePlaceholders(XSLFSlide slide, Map<String, String> replacements) {
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

    static void replacePlaceholders(XSLFSlide slide, String searchText, String replacement) {
        replacePlaceholders(slide, Map.of(searchText, replacement));
    }

    static String findPlaceholderText(XSLFSlide slide, String searchText) {
        for (var placeholder : slide.getPlaceholders()) {
            String text = placeholder.getText();
            if (text.contains(searchText)) {
                return text;
            }
        }
        return null;
    }

    static XSLFSlideMaster getSlideMaster(XMLSlideShow ppt, String name) {
        return ppt.getSlideMasters().stream()
                .filter(master -> name.equals(master.getTheme().getName()))
                .findFirst()
                .orElse(null);
    }

    static XSLFSlideLayout getSlideMasterLayout(XMLSlideShow ppt, String name, String layout) {
        return Optional.ofNullable(getSlideMaster(ppt, name))
                .map(master -> master.getLayout(layout))
                .orElse(null);
    }

    private static void duplicateSlide(XMLSlideShow slides, XSLFSlide srcSlide) {
        var slide = slides.createSlide(srcSlide.getSlideLayout());
        slide.importContent(srcSlide);
    }

    static void generateSlideShow(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath) throws IOException {
        generateSlideShow(contents, tmplFilePath, outputFilePath, PLACEHOLDER_TEMPLATE);
    }

    /**
     * Generate PPTX file based on list of k,v maps and a template file.
     * Template file can have up to 3 template slides:
     * 1. start or content slide
     * 2. content slide
     * 3. end slide
     * Content slide will contain the repeated k,v maps.
     * Note that currently we cannot have multiple styles for different placeholders in the same line. Placeholders
     * need to be delimited by a new line in order of have different styles.
     *
     * @param contents k,v placeholder value maps, each map represent 1 slide.
     *                 First k,v map is the metadata, which will be used for start/end slide if present.
     * @param tmplFilePath template pptx
     * @param outputFilePath output pptx
     * @param placeholderTemplate placeholder format string. e.g. {%s}
     * @throws IOException
     */
    static void generateSlideShow(List<Map<String, String>> contents, String tmplFilePath, String outputFilePath,
            String placeholderTemplate) throws IOException {

        if (contents == null || contents.isEmpty()) {
            copySlideShow(tmplFilePath, outputFilePath);
            return;
        }

        Map<String, String> metadata = contents.get(0);
        contents = contents.subList(1, contents.size());

        List<Map<String, String>> preppedContents = new ArrayList<>();

        // make copies of template slides first and write
        // since for some reason the pptx reference is not able to modify the new copies in memory
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var tmplSlides = new XMLSlideShow(inStream);
            int numTmplSlides = tmplSlides.getSlides().size();
            boolean hasStartSlide = numTmplSlides > 1;
            boolean hasEndSlide = numTmplSlides == 3;

            // start slide if present
            XSLFSlide srcSlide;
            if (hasStartSlide) {
                srcSlide = tmplSlides.getSlides().get(0);
                duplicateSlide(tmplSlides, srcSlide);
                preppedContents.add(metadata);
            }

            // content slides, use all content template slides for each content value map
            int contentTmplStartIndex = hasStartSlide ? 1 : 0;
            int contentTmplEndIndex = hasEndSlide ? numTmplSlides - 1 : numTmplSlides;
            for (int c = 0; c < contents.size(); c++) {
                for (int t = contentTmplStartIndex; t < contentTmplEndIndex; t++) {
                    srcSlide = tmplSlides.getSlides().get(t);
                    duplicateSlide(tmplSlides, srcSlide);
                    preppedContents.add(contents.get(c));
                }
            }

            // end slide if present
            if (hasEndSlide) {
                srcSlide = tmplSlides.getSlides().get(numTmplSlides - 1);
                duplicateSlide(tmplSlides, srcSlide);
                preppedContents.add(metadata);
            }

            // remove template slides
            for (int i = 0; i < numTmplSlides; i++) {
                tmplSlides.removeSlide(0);
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                tmplSlides.write(outStream);
            }
            tmplSlides.close();
        }

        String placeholderTemplateRegex = placeholderTemplate.replace("%s", ".+")
                .replace("{", "\\{")
                .replace("}", "\\}");

        // now do the modifications
        try (var inStream = new FileInputStream(outputFilePath)) {
            var ppt = new XMLSlideShow(inStream);
            for (int i = 0; i < preppedContents.size(); i++) {
                var slide = ppt.getSlides().get(i);
                Map<String, String> values = preppedContents.get(i).entrySet().stream()
                                .collect(Collectors.toMap(e -> placeholderTemplate.formatted(e.getKey()), e -> e.getValue()));
                replaceText(slide, values);
                removeAllTexts(slide, placeholderTemplateRegex);
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    static void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException {
        XMLSlideShow mergedPPT = new XMLSlideShow();
        
        for (String filePath : filePaths) {
            try (var inputStream = new FileInputStream(filePath)) {
                XMLSlideShow ppt = new XMLSlideShow(inputStream);
                for (XSLFSlide slide : ppt.getSlides()) {
                    mergedPPT.createSlide().importContent(slide);
                }
                ppt.close();
            }
        }
        try (var outStream = new FileOutputStream(outputFilePath)) {
            mergedPPT.write(outStream);
        }
        mergedPPT.close();
    }

    static void copySlideShow(String tmplFilePath, String outputFilePath) throws IOException {
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var tmplSlides = new XMLSlideShow(inStream);
            try (var outStream = new FileOutputStream(outputFilePath)) {
                tmplSlides.write(outStream);
            }
            tmplSlides.close();
        }
    }
}
