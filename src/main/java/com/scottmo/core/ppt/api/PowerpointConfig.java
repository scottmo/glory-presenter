package com.scottmo.core.ppt.api;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class PowerpointConfig {
    private Type type;
    private String template;
    private String content;

    public PowerpointConfig() {}

    public PowerpointConfig(Type type, String template, String content) {
        this.type = type;
        this.template = template;
        this.content = content;
    }

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }
    public void setTemplate(String template) {
        this.template = template;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PowerpointConfig{" +
            "type='" + type + '\'' +
            ", template='" + template + '\'' +
            ", content='" + content + '\'' +
            '}';
    }

    public enum Type {
        DEFAULT,
        STYLES,
        SONG,
        BIBLE;

        @JsonCreator
        public static Type fromString(String type) {
            if (type != null) {
                for (Type pptType : Type.values()) {
                    if (pptType.name().equalsIgnoreCase(type)) {
                        return pptType;
                    }
                }
            }
            throw new IllegalArgumentException("No enum constant for type: " + type);
        }

        @JsonValue
        public String toValue() {
            return this.name().toLowerCase();
        }
    }
}
