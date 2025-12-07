package com.scottmo.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LocaleUtilTest {

    @Test
    void normalize_replacesHyphenWithUnderscore() {
        assertEquals("en_us", LocaleUtil.normalize("en-us"));
    }

    @Test
    void normalize_removesSpaces() {
        // spaces are removed entirely, not converted to underscores
        assertEquals("enus", LocaleUtil.normalize("en us"));
        assertEquals("en_us", LocaleUtil.normalize(" en_us "));
    }

    @Test
    void normalize_lowercases() {
        assertEquals("en_us", LocaleUtil.normalize("EN_US"));
        assertEquals("zh_tw", LocaleUtil.normalize("zh-TW"));
    }

    @Test
    void normalize_handlesNull() {
        assertNull(LocaleUtil.normalize(null));
    }

    @Test
    void normalize_combinedTransformations() {
        assertEquals("en_us", LocaleUtil.normalize("  EN-US  "));
    }
}

