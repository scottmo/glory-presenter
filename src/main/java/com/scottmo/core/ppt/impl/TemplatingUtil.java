package com.scottmo.core.ppt.impl;

import org.apache.poi.xslf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
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

    /**
     * Replace all texts in slide matching regex with empty string.
     *
     * @param slide slide to remove texts
     * @param regex regex to match what texts to remove
     */
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

    /**
     * Replace texts in slide matching keys in {@param replacements} with the corresponding values.
     *
     * @param slide slide to replace texts
     * @param replacements replacement texts, keys are the ones to replace and values are the replacements
     */
    static void replaceText(XSLFSlide slide, Map<String, String> replacements) {
        slide.getShapes().stream()
                .filter(s -> s instanceof XSLFTextShape)
                .forEach(s -> replaceText((XSLFTextShape) s, replacements));
    }

    /**
     * Replace texts in shape matching keys in {@param replacements} with the corresponding values.
     *
     * @param shape shape to replace texts
     * @param replacements replacement texts, keys are the ones to replace and values are the replacements
     */
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

    private static boolean isStyleMatched(XSLFTextRun run1, XSLFTextRun run2) {
        if (run1 == null || run2 == null) return false;

        return Objects.equals(run1.getFontColor(), run2.getFontColor())
            && Objects.equals(run1.getFontSize(), run2.getFontSize())
            && Objects.equals(run1.getFontFamily(), run2.getFontFamily())
            && run1.isBold() == run2.isBold()
            && run1.isItalic() == run2.isItalic()
            && run1.isUnderlined() == run2.isUnderlined()
            && run1.isStrikethrough() == run2.isStrikethrough()
            && Objects.equals(run1.getCharacterSpacing(), run2.getCharacterSpacing());
    }

    private static void resetSpacing(XSLFTextParagraph pp) {
        pp.setSpaceBefore(0.0);
        pp.setSpaceAfter(0.0);
        pp.setIndent(0.0);
        pp.setLineSpacing(100.0);
    }

    /**
     * Append text to textbox. Each new line is inserted into its own text run.
     *
     * @param textShape text box to append text
     * @param text text to append
     */
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
     * Set paragraph with text. Each new line is inserted into its own text run. Original font styling is used.
     *
     * @param pp paragraph object to set text
     * @param text text to set
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
            copyStyles(newTextRun, baseTextRun);
        }
        resetSpacing(pp);
    }

    private static void copyStyles(XSLFTextRun run1, XSLFTextRun run2) {
        if (run1 == null || run2 == null) return;

        run1.setFontColor(run2.getFontColor());
        run1.setFontFamily(run2.getFontFamily());
        run1.setFontSize(run2.getFontSize());
        run1.setBold(run2.isBold());
        run1.setItalic(run2.isItalic());
        run1.setUnderlined(run2.isUnderlined());
        run1.setStrikethrough(run2.isStrikethrough());
        run1.setCharacterSpacing(run2.getCharacterSpacing());
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
            if (!"\n".equals(textRun.getRawText())) {
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

    private static void duplicateSlide(XMLSlideShow slides, XSLFSlide srcSlide, boolean isExternal) {
        var slideLayout = srcSlide.getSlideLayout();
        if (isExternal) {
            slideLayout = findMatchingLayout(slides, slideLayout);
        }
        var toSlide = slides.createSlide(slideLayout);
        toSlide.getBackground().setFillColor(srcSlide.getBackground().getFillColor());
        toSlide.importContent(srcSlide);
    }

    private static XSLFSlideLayout findMatchingLayout(XMLSlideShow slides, XSLFSlideLayout srcLayout) {
        for (XSLFSlideMaster master : slides.getSlideMasters()) {
            for (XSLFSlideLayout layout : master.getSlideLayouts()) {
                if (layout.getName().equals(srcLayout.getName())) {
                    return layout;
                }
            }
        }
        // If no matching layout found, return the first available layout
        return slides.getSlideMasters().get(0).getSlideLayouts()[0];
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

            // start slide if present
            XSLFSlide srcSlide;

            // start slide
            srcSlide = tmplSlides.getSlides().get(0);
            duplicateSlide(tmplSlides, srcSlide, false);
            preppedContents.add(metadata);

            // content slides, use all content template slides for each content value map
            int contentTmplStartIndex = 1;
            int contentTmplEndIndex = numTmplSlides - 1;
            for (Map<String, String> content : contents) {
                for (int t = contentTmplStartIndex; t < contentTmplEndIndex; t++) {
                    srcSlide = tmplSlides.getSlides().get(t);
                    duplicateSlide(tmplSlides, srcSlide, false);
                    preppedContents.add(content);
                }
            }

            // end slide
            srcSlide = tmplSlides.getSlides().get(numTmplSlides - 1);
            duplicateSlide(tmplSlides, srcSlide, false);
            preppedContents.add(metadata);

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
        loadSlideShow(outputFilePath, ppt -> {
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
        });
    }

    static void mergeSlideShows(List<String> filePaths, String outputFilePath) throws IOException {
        // use first src as base to maintain ppt size and other attributes
        String basePPTPath = filePaths.get(0);
        List<String> restPPTPaths =  filePaths.subList(1, filePaths.size());;
        loadSlideShow(basePPTPath, mergedPPT -> {
            for (String filePath : restPPTPaths) {
                loadSlideShow(filePath, ppt -> {
                    for (XSLFSlide srcSlide : ppt.getSlides()) {
                        duplicateSlide(mergedPPT, srcSlide, true);
                    }
                });
            }
            try (var outStream = new FileOutputStream(outputFilePath)) {
                mergedPPT.write(outStream);
            }
        });
    }

    static void loadSlideShow(String filePath, IOConsumer<XMLSlideShow> onLoad) throws IOException {
        try (var is = new FileInputStream(filePath)) {
            XMLSlideShow ppt = new XMLSlideShow(is);
            onLoad.accept(ppt);
            ppt.close();
        }
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

    /**
     * Splits all paragraphs within the given XSLFTextShape that contain newline characters
     * into multiple, correctly styled XSLFTextParagraphs.
     * This is useful because sometimes line breaks don't display correctly in pptx.
     *
     * @param shape The XSLFTextShape whose content needs to be processed and reorganized.
     */
    static void convertNewlinesIntoParagraphs(XSLFTextShape shape) {
        if (shape == null) return;

        List<ParagraphData> paragraphDataList = new ArrayList<>();
        for (XSLFTextParagraph p : shape.getTextParagraphs()) {
            if (!p.getTextRuns().isEmpty()) {
                String text = p.getText();
                XSLFTextRun textRun = p.getTextRuns().get(0); // use it for styling
                String[] lines = text.split("\n");

                for (String line : lines) {
                    ParagraphData paragraphData = new ParagraphData(p, textRun, line);
                    paragraphDataList.add(paragraphData);
                }
            }
        }

        shape.clearText();
        for (ParagraphData paragraphData : paragraphDataList) {
            XSLFTextParagraph p = shape.addNewTextParagraph();
            paragraphData.apply(p);
        }
    }

    @FunctionalInterface
    interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }
}
