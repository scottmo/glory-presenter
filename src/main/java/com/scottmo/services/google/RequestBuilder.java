package com.scottmo.services.google;

import com.google.api.services.slides.v1.model.*;
import com.scottmo.services.google.SlideConfig.ParagraphConfig;
import com.scottmo.services.google.SlideConfig.TextConfig;
import com.scottmo.util.StringSegment;
import com.scottmo.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RequestBuilder {

    public static final String ID_SHAPE_PREFIX = "o";
    public static final String ID_SLIDE_PREFIX = "s";
    public static final String ID_PLACEHOLDER_PREFIX = "p";

    private final List<Request> requests = new ArrayList<>();

    public List<Request> build() {
        return requests;
    }

    /**
     * set base font for a slide
     */
    public void setBaseFont(Page slide, Map<String, TextConfig> textConfigs) {
        slide.getPageElements().stream()
                .filter(pageElement -> pageElement.getObjectId() != null)
                .forEach(pageElement -> {
                    SlidesUtil.getTextElements(pageElement).stream()
                            .filter(textElement -> textElement.getTextRun() != null)
                            .forEach(textElement -> {
                                setBaseFontForText(pageElement.getObjectId(), textElement.getTextRun(),
                                        textConfigs, textElement.getStartIndex());
                            });
                });
    }

    /**
     * set base font for a text run
     */
    private void setBaseFontForText(String pageElementId, TextRun textRun,
                                    Map<String, TextConfig> textConfigs, int startIndex) {
        Optional.ofNullable(textRun.getContent()).ifPresent(content -> {
            StringUtils.splitByCharset(content, true).forEach(contentSegment -> {
                String textConfigName = getTextConfigName(contentSegment);
                TextConfig textConfig = textConfigs.get(textConfigName);
                requests.add(new Request()
                        .setUpdateTextStyle(new UpdateTextStyleRequest()
                                .setObjectId(pageElementId)
                                .setFields("*")
                                .setTextRange(SlidesUtil.getTextRange(
                                        startIndex + contentSegment.startIndex(),
                                        startIndex + contentSegment.endIndex()
                                ))
                                .setStyle(textRun.getStyle().clone()
                                        .setForegroundColor(new OptionalColor()
                                                .setOpaqueColor(SlidesUtil.getRGBColor(textConfig.getFontColor())))
                                        .setFontFamily(textConfig.getFontFamily())
                                        .setWeightedFontFamily(textRun.getStyle().getWeightedFontFamily().clone()
                                                .setFontFamily(textConfig.getFontFamily())))));
            });
        });
    }

    public void resizeToFullPage(String pageElementId) {
        requests.add(new Request()
                .setUpdatePageElementTransform(new UpdatePageElementTransformRequest()
                        .setObjectId(pageElementId)
                        .setTransform(new AffineTransform()
                                .setScaleX(DefaultSlideConfig.SLIDE_W / DefaultSlideConfig.SLIDE_BASE)
                                .setScaleY(DefaultSlideConfig.SLIDE_H / DefaultSlideConfig.SLIDE_BASE)
                                .setUnit("PT"))
                        .setApplyMode("ABSOLUTE")));
    }

    public String createTextBox(String pageElementId,
                                Double w, Double h, Double tx, Double ty) {
        String textBoxId = SlidesUtil.generateObjectId(ID_SHAPE_PREFIX);
        return createTextBox(textBoxId, pageElementId, w, h, tx, ty);
    }

    private String createTextBox(String textBoxId, String pageElementId,
                                 Double w, Double h, Double tx, Double ty) {
        requests.add(new Request()
                .setCreateShape(new CreateShapeRequest()
                        .setObjectId(textBoxId)
                        .setShapeType("TEXT_BOX")
                        .setElementProperties(new PageElementProperties()
                                .setPageObjectId(pageElementId)
                                .setSize(new Size()
                                        .setWidth(SlidesUtil.getDimension(w))
                                        .setHeight(SlidesUtil.getDimension(h)))
                                .setTransform(new AffineTransform()
                                        .setScaleX(1.0)
                                        .setScaleY(1.0)
                                        .setTranslateX(tx)
                                        .setTranslateY(ty)
                                        .setUnit("PT")))));
        return textBoxId;
    }

    public void insertText(String textBoxId, String textContent,
                           ParagraphConfig paragraphConfig, TextConfig textConfig) {
        insertText(textBoxId, textContent, paragraphConfig, textConfig, 0);
    }

    public void insertText(String textBoxId, String textContent,
                           ParagraphConfig paragraphConfig, TextConfig textConfig,
                           int textInsertionIndex) {
        // text
        requests.add(new Request()
                .setInsertText(new InsertTextRequest()
                        .setObjectId(textBoxId)
                        .setText(textContent)
                        .setInsertionIndex(textInsertionIndex)));

        Range insertTextRange = SlidesUtil.getTextRange(textInsertionIndex, textInsertionIndex + textContent.length());

        // paragraph style
        boolean hasParagraphStyle = false;
        ParagraphStyle ppStyle = new ParagraphStyle();
        if (!paragraphConfig.getAlignment().isEmpty()) {
            hasParagraphStyle = true;
            ppStyle.setAlignment(paragraphConfig.getAlignment());
        }
        if (paragraphConfig.getIndentation() > 0) {
            hasParagraphStyle = true;
            Dimension indent = SlidesUtil.getDimension(paragraphConfig.getIndentation());
            ppStyle.setIndentFirstLine(indent)
                    .setIndentStart(indent)
                    .setIndentEnd(indent);
        }

        if (hasParagraphStyle) {
            requests.add(new Request()
                    .setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
                            .setObjectId(textBoxId)
                            .setStyle(ppStyle)
                            .setFields("*")
                            .setTextRange(insertTextRange)));
        }

        // text style
        boolean hasTextStyle = false;
        TextStyle textStyle = new TextStyle();
        if (!textConfig.getFontStyles().isEmpty()) {
            hasTextStyle = true;
            textStyle.setSmallCaps(textConfig.getFontStyles().contains("smallCaps"))
                    .setStrikethrough(textConfig.getFontStyles().contains("strikethrough"))
                    .setUnderline(textConfig.getFontStyles().contains("underline"))
                    .setBold(textConfig.getFontStyles().contains("bold"))
                    .setItalic(textConfig.getFontStyles().contains("italic"));
        }
        if (!textConfig.getFontColor().isEmpty()) {
            hasTextStyle = true;
            textStyle.setForegroundColor(new OptionalColor()
                    .setOpaqueColor(SlidesUtil.getRGBColor(textConfig.getFontColor())));
        }
        if (textConfig.getFontSize() > 0) {
            hasTextStyle = true;
            textStyle.setFontSize(SlidesUtil.getDimension(textConfig.getFontSize()));
        }
        if (!textConfig.getFontFamily().isEmpty()) {
            hasTextStyle = true;
            textStyle.setFontFamily(textConfig.getFontFamily());
        }
        if (textConfig.getFontStyles().contains("bold")) {
            textStyle.setWeightedFontFamily(new WeightedFontFamily()
                    .setFontFamily(textConfig.getFontFamily())
                    .setWeight(700));
        }

        if (hasTextStyle) {
            requests.add(new Request()
                    .setUpdateTextStyle(new UpdateTextStyleRequest()
                            .setObjectId(textBoxId)
                            .setStyle(textStyle)
                            .setFields("*")
                            .setTextRange(insertTextRange)));
        }
    }

    /**
     * Insert multilingual texts.
     */
    public void insertText(String textBoxId, Map<String, String> textConfig, SlideConfig slideConfig) {
        // we always insert from the top of the text box, so reverse the list and when inserting,
        // we push the text down
        List<String> textConfigsOrder = slideConfig.getTextConfigsOrder().stream()
                .sorted(Collections.reverseOrder())
                .filter(textConfig::containsKey)
                .toList();
        for (int i = 0; i < textConfigsOrder.size(); i++) {
            String configName = textConfigsOrder.get(i);
            String ln = i == 0 ? "" : "\n";
            insertText(textBoxId, textConfig.get(configName) + ln,
                    slideConfig.getParagraph(), slideConfig.getTextConfigs().get(configName));
        }
    }

    public String createText(String pageElementId, String textContent,
                             ParagraphConfig paragraphConfig, TextConfig textConfig, boolean isFullPage) {
        double textBoxW = DefaultSlideConfig.SLIDE_W;
        double textBoxH = (isFullPage || textConfig.getFontSize() <= 0)
                ? DefaultSlideConfig.SLIDE_H
                : textConfig.getFontSize() * 2;

        String textBoxId = createTextBox(pageElementId, textBoxW, textBoxH, paragraphConfig.getX(), paragraphConfig.getY());
        insertText(textBoxId, textContent, paragraphConfig, textConfig);
        return textBoxId;
    }

    private void copyText(PageElement srcElement, PageElement dstElement, boolean withStyles) {
        // copy texts
        srcElement.getShape().getText().getTextElements().stream()
                .filter(textElement -> Optional.ofNullable(textElement.getTextRun()).map(TextRun::getContent).isPresent())
                .forEach(textElement -> {
                    requests.add(new Request()
                            .setInsertText(new InsertTextRequest()
                                    .setObjectId(dstElement.getObjectId())
                                    .setText(textElement.getTextRun().getContent())
                                    .setInsertionIndex(textElement.getStartIndex())));
                });

        if (!withStyles) {
            return;
        }

        // copy styles
        srcElement.getShape().getText().getTextElements()
                .forEach(textElement -> {
                    Range textRange = SlidesUtil.getTextRange(textElement.getStartIndex(), textElement.getEndIndex());
                    if (textElement.getParagraphMarker() != null) {
                        requests.add(new Request()
                                .setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
                                        .setObjectId(dstElement.getObjectId())
                                        .setStyle(textElement.getParagraphMarker().getStyle())
                                        .setFields("*")
                                        .setTextRange(textRange)));
                    } else {
                        requests.add(new Request()
                                .setUpdateTextStyle(new UpdateTextStyleRequest()
                                        .setObjectId(dstElement.getObjectId())
                                        .setStyle(textElement.getTextRun().getStyle())
                                        .setFields("*")
                                        .setTextRange(textRange)));
                    }
                });
    }

    private void copyShape(PageElement srcElement, PageElement dstElement) {
        Optional.ofNullable(srcElement.getShape())
                .map(Shape::getShapeProperties)
                .ifPresent(srcShapeProperties -> {
                    ShapeProperties copiedShapeProps = new ShapeProperties()
                            .setContentAlignment(srcShapeProperties.getContentAlignment())
                            .setLink(srcShapeProperties.getLink());
                    if (!srcShapeProperties.getOutline().getPropertyState().equals("NOT_RENDERED")) {
                        copiedShapeProps.setOutline(srcShapeProperties.getOutline());
                    }
                    if (!srcShapeProperties.getShapeBackgroundFill().getPropertyState().equals("NOT_RENDERED")) {
                        copiedShapeProps.setShapeBackgroundFill(srcShapeProperties.getShapeBackgroundFill());
                    }
                    // ignore autofit and shadow
                    requests.add(new Request()
                            .setUpdateShapeProperties(new UpdateShapePropertiesRequest()
                                    .setObjectId(dstElement.getObjectId())
                                    .setShapeProperties(copiedShapeProps)));
                });
    }

    private void copyTransform(PageElement srcElement, PageElement dstElement) {
        requests.add(new Request()
                .setUpdatePageElementTransform(new UpdatePageElementTransformRequest()
                        .setObjectId(dstElement.getObjectId())
                        .setTransform(srcElement.getTransform())
                        .setApplyMode("ABSOLUTE")));
    }

    private void deleteObject(String id) {
        requests.add(new Request()
                .setDeleteObject(new DeleteObjectRequest()
                        .setObjectId(id)));
    }

    public String createSlide(int slideIndex) {
        String slideId = SlidesUtil.generateObjectId(ID_SLIDE_PREFIX);
        return this.createSlide(slideIndex, slideId);
    }

    private String createSlide(int slideIndex, String slideId) {
        requests.add(new Request()
                .setCreateSlide(new CreateSlideRequest()
                        .setObjectId(slideId)
                        .setInsertionIndex(slideIndex)
                        .setSlideLayoutReference(new LayoutReference()
                                .setPredefinedLayout("TITLE_ONLY"))
                        .setPlaceholderIdMappings(List.of(new LayoutPlaceholderIdMapping()
                                .setObjectId(getPlaceHolderId(slideId))
                                .setLayoutPlaceholder(new Placeholder()
                                        .setType("TITLE"))))));
        return slideId;
    }

    public String getPlaceHolderId(String slideId) {
        return ID_PLACEHOLDER_PREFIX + "-" + slideId;
    }

    /**
     * Create a slide with title text resized to full slide and return its id
     *
     * @return text box objectId
     */
    public String createSlideWithFullText(int slideIndex) {
        String slideId = this.createSlide(slideIndex);
        String titleId = this.getPlaceHolderId(slideId);
        this.resizeToFullPage(titleId);
        return titleId;
    }

    public void setDefaultTitleText(Page slide) {
        SlidesUtil.getFirstText(slide).ifPresent(firstText -> {
            if (firstText.getObjectId() == null) return;

            SlidesUtil.getTitlePlaceholder(slide).ifPresent(title -> {
                if (firstText == title) return;

                this.copyText(firstText, title, true);
                this.copyShape(firstText, title);
                this.copyTransform(firstText, title);
                this.deleteObject(firstText.getObjectId());
            });
        });
    }

    /**
     * use to match slide configuration. convenient method for CJK languages.
     */
    private String getTextConfigName(StringSegment segment) {
        // FIXME
        return segment.isAscii() ? "en_us" : "zh_cn";
    }
}
