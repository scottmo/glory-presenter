package com.scottmo.core.songs.api.song;

import java.util.Locale;

import com.scottmo.shared.LocaleUtil;

public class SongTitle {

    private String text;
    private String locale;

    public SongTitle() {}

    public SongTitle(String text, String locale) {
        this.text = text;
        this.locale = LocaleUtil.normalize(locale);
    }

    public SongTitle(String text) {
        this(text, Locale.getDefault().toString());
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = LocaleUtil.normalize(locale);
    }
}
