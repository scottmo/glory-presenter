package com.scottscmo.ui.components;

public record FormInput(
        String id,
        String label,
        String type,
        String defaultValue,
        int height,
        int width
) {
    public FormInput(String id, String label) {
        this(id, label, "text");
    }

    public FormInput(String id, String label, String type) {
        this(id, label, type, "");
    }

    public FormInput(String id, String label, String type, String defaultValue) {
        this(id, label, type, defaultValue, 10, 10);
    }
}
