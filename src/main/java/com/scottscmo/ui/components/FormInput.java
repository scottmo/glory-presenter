package com.scottscmo.ui.components;

public record FormInput(
        String label,
        String type,
        String defaultValue,
        int height,
        int width
) {
    public FormInput(String label) {
        this(label, "text");
    }

    public FormInput(String label, String type) {
        this(label, type, "");
    }

    public FormInput(String label, String type, String defaultValue) {
        this(label, type, defaultValue, 10, 10);
    }
}
