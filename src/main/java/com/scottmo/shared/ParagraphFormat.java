package com.scottmo.shared;

import java.util.Map;

public class ParagraphFormat {
    private final String alignment;
    private final double indentation;
    private final Dimension dimension;
    private final Map<String, TextFormat> textFormats;

    public ParagraphFormat(String alignment, double indentation, Dimension dimension,
            Map<String, TextFormat> font) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.dimension = dimension;
        this.textFormats = font;
    }

    public String getAlignment() {
        return alignment;
    }
    public double getIndentation() {
        return indentation;
    }
    public Dimension getDimension() {
        return dimension;
    }
    public Map<String, TextFormat> getTextFormats() {
        return textFormats;
    }
}

