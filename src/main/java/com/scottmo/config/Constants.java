package com.scottmo.config;

import com.scottmo.util.LocaleUtil;

import java.util.Locale;

public class Constants {
    public static final String APP_NAME = "Glory Presenter";

    // TODO: make this configurable
    public static final String PRIMARY_LOCALE = LocaleUtil.DEFAULT_LOCALE;
    public static final String SECONDARY_LOCALE = LocaleUtil.normalize("zh_CN");
}
