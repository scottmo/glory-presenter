package com.scottscmo.google;

import com.google.api.services.slides.v1.model.*;
import com.scottscmo.Config;
import com.scottscmo.util.StringSegment;
import com.scottscmo.util.StringUtils;
import com.scottscmo.config.ParagraphConfig;
import com.scottscmo.config.SlideConfig;
import com.scottscmo.config.TextConfig;

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
                    Util.getTextElements(pageElement).stream()
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
                                .setTextRange(Util.getTextRange(
                                        startIndex + contentSegment.startIndex(),
                                        startIndex + contentSegment.endIndex()
                                ))
                                .setStyle(textRun.getStyle().clone()
                                        .setForegroundColor(new OptionalColor()
                                                .setOpaqueColor(Util.getRGBColor(textConfig.fontColor())))
                                        .setFontFamily(textConfig.fontFamily())
                                        .setWeightedFontFamily(textRun.getStyle().getWeightedFontFamily().clone()
                                                .setFontFamily(textConfig.fontFamily())))));
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
        String textBoxId = Util.generateObjectId(ID_SHAPE_PREFIX);
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
                                        .setWidth(Util.getDimension(w))
                                        .setHeight(Util.getDimension(h)))
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

        Range insertTextRange = Util.getTextRange(textInsertionIndex, textInsertionIndex + textContent.length());

        // paragraph style
        boolean hasParagraphStyle = false;
        ParagraphStyle ppStyle = new ParagraphStyle();
        if (!paragraphConfig.alignment().isEmpty()) {
            hasParagraphStyle = true;
            ppStyle.setAlignment(paragraphConfig.alignment());
        }
        if (paragraphConfig.indentation() > 0) {
            hasParagraphStyle = true;
            Dimension indent = Util.getDimension(paragraphConfig.indentation());
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
        if (!textConfig.fontStyles().isEmpty()) {
            hasTextStyle = true;
            textStyle.setSmallCaps(textConfig.fontStyles().contains("smallCaps"))
                    .setStrikethrough(textConfig.fontStyles().contains("strikethrough"))
                    .setUnderline(textConfig.fontStyles().contains("underline"))
                    .setBold(textConfig.fontStyles().contains("bold"))
                    .setItalic(textConfig.fontStyles().contains("italic"));
        }
        if (!textConfig.fontColor().isEmpty()) {
            hasTextStyle = true;
            textStyle.setForegroundColor(new OptionalColor()
                    .setOpaqueColor(Util.getRGBColor(textConfig.fontColor())));
        }
        if (textConfig.fontSize() > 0) {
            hasTextStyle = true;
            textStyle.setFontSize(Util.getDimension(textConfig.fontSize()));
        }
        if (!textConfig.fontFamily().isEmpty()) {
            hasTextStyle = true;
            textStyle.setFontFamily(textConfig.fontFamily());
        }
        if (textConfig.fontStyles().contains("bold")) {
            textStyle.setWeightedFontFamily(new WeightedFontFamily()
                    .setFontFamily(textConfig.fontFamily())
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
        List<String> textConfigsOrder = slideConfig.textConfigsOrder().stream()
                .sorted(Collections.reverseOrder())
                .filter(textConfig::containsKey)
                .toList();
        // run in reverse order since we insert by pushing text down
        // doing it sequentially will require us to calculate the length of inserted text which can be inconsistent
        // with new lines
        for (int i = textConfigsOrder.size() - 1; i >= 0; i--) {
            String configName = textConfigsOrder.get(i);
            String ln = i == textConfigsOrder.size() - 1 ? "" : "\n";
            insertText(textBoxId, textConfig.get(configName) + ln,
                    slideConfig.paragraph(), slideConfig.textConfigs().get(configName));
        }
    }

    public String createText(String pageElementId, String textContent,
                             ParagraphConfig paragraphConfig, TextConfig textConfig, boolean isFullPage) {
        double textBoxW = DefaultSlideConfig.SLIDE_W;
        double textBoxH = (isFullPage || textConfig.fontSize() <= 0)
                ? DefaultSlideConfig.SLIDE_H
                : textConfig.fontSize() * 2;

        String textBoxId = createTextBox(pageElementId, textBoxW, textBoxH, paragraphConfig.x(), paragraphConfig.y());
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
                    Range textRange = Util.getTextRange(textElement.getStartIndex(), textElement.getEndIndex());
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
        String slideId = Util.generateObjectId(ID_SLIDE_PREFIX);
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
        Util.getFirstText(slide).ifPresent(firstText -> {
            if (firstText.getObjectId() == null) return;

            Util.getTitlePlaceholder(slide).ifPresent(title -> {
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
        SlideConfig slideConfig = Config.get().googleSlideConfig();
        return segment.isAscii() ? slideConfig.defaultAsciiTextConfig() : slideConfig.defaultNonAsciiTextConfig();
    }
}
