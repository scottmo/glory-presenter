package com.scottmo.services.google;

import java.util.Objects;

public class ParagraphConfig {
    private final String alignment;
    private final double indentation;
    private final double x;
    private final double y;

    public ParagraphConfig(String alignment, double indentation, double x, double y) {
        this.alignment = alignment;
        this.indentation = indentation;
        this.x = x;
        this.y = y;
    }

    public String getAlignment() {
        return alignment;
    }

    public double getIndentation() {
        return indentation;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "ParagraphConfig{" +
                "alignment='" + alignment + '\'' +
                ", indentation=" + indentation +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParagraphConfig that = (ParagraphConfig) o;

        if (Double.compare(that.indentation, indentation) != 0) return false;
        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        return alignment.equals(that.alignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alignment, indentation, x, y);
    }
}
