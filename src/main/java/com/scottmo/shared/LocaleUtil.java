package com.scottmo.shared;

import java.util.Locale;

public class LocaleUtil {
    public static final String DEFAULT_LOCALE = normalize(Locale.getDefault().toString());

    public static String normalize(String locale) {
        if (locale == null) return null;
        return locale.replace("-", "_")
                .replace(" ", "")
                .toLowerCase().trim();
    }
}
