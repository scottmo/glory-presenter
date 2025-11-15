package com.scottmo.core.ppt.impl;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.awt.*;

public class ParagraphData {
    private boolean isBullet;
    private String bulletCharacter;
    private Double lineSpacing;
    private Double spaceAfter;
    private Double spaceBefore;
    private Double indent;
    private int indentLevel;
    private Double leftMargin;
    private Double rightMargin;
    private TextParagraph.TextAlign textAlign;
    private String bulletFont;
    private PaintStyle bulletFontColor;
    private TextParagraph.FontAlign fontAlign;
    private TextParagraph.BulletStyle bulletStyle;

    private TextRunData textRunData;
    private String text;

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

    public void apply(XSLFTextParagraph p) {
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

        p.setBullet(this.isBullet);
        if (this.isBullet) {
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

    private static class TextRunData {
        private PaintStyle fontColor;
        private String fontFamily;
        private Double characterSpacing;
        private Double fontSize;
        private PaintStyle highlightColor;
        private boolean bold;
        private boolean italic;
        private boolean strikethrough;
        private boolean subscript;
        private boolean superscript;
        private boolean underlined;

        public TextRunData(XSLFTextRun textRun) {
            this.fontColor = textRun.getFontColor();
            this.fontFamily = textRun.getFontFamily();
            this.characterSpacing = textRun.getCharacterSpacing();
            this.fontSize = textRun.getFontSize();
            this.highlightColor = textRun.getHighlightColor();
            this.bold = textRun.isBold();
            this.italic = textRun.isItalic();
            this.strikethrough = textRun.isStrikethrough();
            this.subscript = textRun.isSubscript();
            this.superscript = textRun.isSuperscript();
            this.underlined = textRun.isUnderlined();
        }

        public void apply(XSLFTextRun run) {
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
