package com.scottmo.shared;

public class StringSegment {

    private int startIndex;
    private int endIndex;
    private String value;
    private boolean isAscii;

    public StringSegment(int startIndex, int endIndex, String value, boolean isAscii) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = value;
        this.isAscii = isAscii;
    }

    public int startIndex() {
        return startIndex;
    }
    public void startIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int endIndex() {
        return endIndex;
    }
    public void endIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String value() {
        return value;
    }
    public void value(String value) {
        this.value = value;
    }

    public boolean isAscii() {
        return isAscii;
    }
    public void isAscii(boolean isAscii) {
        this.isAscii = isAscii;
    }
}
