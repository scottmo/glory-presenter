package com.scottmo.services.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class TemplatingUtil {
    static final String PLACEHOLDER_TEMPLATE = "{%s}";

    public static String getText(XSLFSlide slide) {
        return slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .map(s -> ((XSLFTextShape)s).getTextParagraphs().stream()
                    .map(XSLFTextParagraph::getText)
                    .collect(Collectors.joining()))
                .collect(Collectors.joining("\n"));
    }

    public static void removeAllTexts(XSLFSlide slide, String regex) {
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
        textShape.getTextParagraphs().forEach(TemplatingUtil::clearText);
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

    private static void duplicateSlide(XMLSlideShow slides, XSLFSlide srcSlide) {
        var slide = slides.createSlide(srcSlide.getSlideLayout());
        slide.importContent(srcSlide);
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
     *                 First k,v map will be used as start slide if present.
     *                 Last k,v map will be used as end slide if present.
     * @param placeholderTemplate placeholder format string. e.g. {%s}
     * @param tmplFilePath template pptx
     * @param outputFilePath output pptx
     * @throws IOException
     */
    public static void generateSlideShow(List<Map<String, String>> contents, boolean hasStartContent, boolean hasEndContent,
                                         String placeholderTemplate, String tmplFilePath, String outputFilePath) throws IOException {
        // make copies of template slides first and write
        // since for some reason the pptx reference is not able to modify the new copies in memory
        try (var inStream = new FileInputStream(tmplFilePath)) {
            var tmplSlides = new XMLSlideShow(inStream);
            int numPlaceholderSlides = tmplSlides.getSlides().size();
            // start slide if present
            XSLFSlide srcSlide;
            if (hasStartContent) {
                srcSlide = tmplSlides.getSlides().get(0);
                duplicateSlide(tmplSlides, srcSlide);
            }

            // content slides, alternate each content template slide if there are multiple
            int contentStartIndex = hasStartContent ? 1 : 0;
            int contentEndIndex = hasEndContent ? contents.size() - 1 : contents.size();
            int contentTmplEndIndex = hasEndContent ? numPlaceholderSlides - 1 : numPlaceholderSlides;
            int contentTmplSlideIndex = contentStartIndex;
            for (int j = contentStartIndex; j < contentEndIndex; j++) {
                srcSlide = tmplSlides.getSlides().get(contentTmplSlideIndex);
                duplicateSlide(tmplSlides, srcSlide);
                contentTmplSlideIndex++;
                if (contentTmplSlideIndex == contentTmplEndIndex) {
                    contentTmplSlideIndex = contentStartIndex;
                }
            }

            // end slide if present
            if (hasEndContent) {
                srcSlide = tmplSlides.getSlides().get(numPlaceholderSlides - 1);
                duplicateSlide(tmplSlides, srcSlide);
            }

            // remove template slides
            for (int i = 0; i < numPlaceholderSlides; i++) {
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
            for (int i = 0; i < contents.size(); i++) {
                var slide = ppt.getSlides().get(i);
                Map<String, String> values = contents.get(i).entrySet().stream()
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
}
