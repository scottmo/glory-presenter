package com.scottmo.core.ppt.impl;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to process an XSLFTextShape and convert newline characters ("\n", "\r", "\r\n")
 * found within text runs into distinct XSLFTextParagraph elements, while preserving formatting.
 */
public class NewLineUtil {

    /**
     * Splits all paragraphs within the given XSLFTextShape that contain newline characters
     * into multiple, correctly styled XSLFTextParagraphs.
     * * @param shape The XSLFTextShape whose content needs to be processed and reorganized.
     */
    public static void splitNewlinesIntoParagraphs(XSLFTextShape shape) {
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

    // --- Helper Class for Clean Data Rebuilding ---
    private static class ParagraphData {
        boolean isBullet;
        String bulletCharacter;
        Double lineSpacing;
        Double spaceAfter;
        Double spaceBefore;
        Double indent;
        int indentLevel;
        Double leftMargin;
        Double rightMargin;
        TextParagraph.TextAlign textAlign;
        String bulletFont;
        PaintStyle bulletFontColor;
        TextParagraph.FontAlign fontAlign;
        TextParagraph.BulletStyle bulletStyle;

        TextRunData textRunData;
        String text;

        public ParagraphData(XSLFTextParagraph p, XSLFTextRun r, String text) {
            this.lineSpacing = p.getLineSpacing();
            this.spaceAfter = p.getSpaceAfter();
            this.spaceBefore = p.getSpaceBefore();
            this.indent = p.getIndent();
            this.indentLevel = p.getIndentLevel();
            this.leftMargin = p.getLeftMargin();
            this.rightMargin = p.getRightMargin();
            this.textAlign = p.getTextAlign();
            this.fontAlign = p.getFontAlign();

            if (p.isBullet()) {
                this.isBullet = p.isBullet();
                this.bulletCharacter = p.getBulletCharacter();
                this.bulletFont = p.getBulletFont();
                this.bulletFontColor = p.getBulletFontColor();
                this.bulletStyle = p.getBulletStyle();
            }
            this.text = text;
            this.textRunData = new TextRunData(r);
        }

        void apply(XSLFTextParagraph p) {
            p.setLineSpacing(this.lineSpacing);
            p.setSpaceAfter(this.spaceAfter);
            p.setSpaceBefore(this.spaceBefore);
            p.setIndent(this.indent);
            p.setIndentLevel(this.indentLevel);
            p.setLeftMargin(this.leftMargin);
            p.setRightMargin(this.rightMargin);
            if (textAlign != null)
                p.setTextAlign(this.textAlign);
            if (fontAlign != null)
                p.setFontAlign(this.fontAlign);

            if (this.isBullet) {
                p.setBullet(this.isBullet);
                if (bulletFont != null)
                    p.setBulletFont(this.bulletFont);
                if (bulletFontColor != null)
                    p.setBulletFontColor(this.bulletFontColor);
                if (bulletStyle != null)
                    p.setBulletStyle(this.bulletStyle);
                if (bulletCharacter != null)
                    p.setBulletCharacter(this.bulletCharacter);
            }

            XSLFTextRun run = p.addNewTextRun();
            run.setText(this.text);
            this.textRunData.apply(run);
        }
    }

    private static class TextRunData {
        PaintStyle fontColor;
        String fontFamily;
        Double characterSpacing;
        Double fontSize;
        PaintStyle highlightColor;
        boolean bold;
        boolean italic;
        boolean strikethrough;
        boolean subscript;
        boolean superscript;
        boolean underlined;

        public TextRunData(XSLFTextRun run) {
            this.fontColor = run.getFontColor();
            this.fontFamily = run.getFontFamily();
            this.characterSpacing = run.getCharacterSpacing();
            this.fontSize = run.getFontSize();
            this.highlightColor = run.getHighlightColor();
            this.bold = run.isBold();
            this.italic = run.isItalic();
            this.strikethrough = run.isStrikethrough();
            this.subscript = run.isSubscript();
            this.superscript = run.isSuperscript();
            this.underlined = run.isUnderlined();
        }

        void apply(XSLFTextRun run) {
            if (fontColor != null)
                run.setFontColor(getColor(fontColor));
            if (fontFamily != null)
                run.setFontFamily(this.fontFamily);
            if (characterSpacing != null)
                run.setCharacterSpacing(this.characterSpacing);
            if (fontSize != null)
                run.setFontSize(this.fontSize);
            if (highlightColor != null)
                run.setHighlightColor(getColor(highlightColor));
            run.setBold(this.bold);
            run.setItalic(this.italic);
            run.setStrikethrough(this.strikethrough);
            run.setSubscript(this.subscript);
            run.setSuperscript(this.superscript);
            run.setUnderlined(this.underlined);
        }

        private Color getColor(PaintStyle color) {
            // for some reason setColor fails at calculating alpha :/
            return ((PaintStyle.SolidPaint) color).getSolidColor().getColor();
        }
    }
}
