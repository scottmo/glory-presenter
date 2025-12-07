package com.scottmo.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class StringUtilsTest {

    // isASCII tests
    @Test
    void isASCII_returnsTrue_forAsciiString() {
        assertTrue(StringUtils.isASCII("hello world"));
        assertTrue(StringUtils.isASCII("123!@#"));
    }

    @Test
    void isASCII_returnsFalse_forNonAsciiString() {
        assertFalse(StringUtils.isASCII("你好"));
        assertFalse(StringUtils.isASCII("hello世界"));
    }

    // isPunctuationOrDigit tests
    @Test
    void isPunctuationOrDigit_returnsTrue_forPunctuationOrDigits() {
        assertTrue(StringUtils.isPunctuationOrDigit("123"));
        assertTrue(StringUtils.isPunctuationOrDigit("!@#"));
        assertTrue(StringUtils.isPunctuationOrDigit("1."));
    }

    @Test
    void isPunctuationOrDigit_returnsFalse_forLetters() {
        assertFalse(StringUtils.isPunctuationOrDigit("abc"));
        assertFalse(StringUtils.isPunctuationOrDigit("a1"));
    }

    // splitByCharset tests
    @Test
    void splitByCharset_separatesAsciiAndNonAscii() {
        List<StringSegment> result = StringUtils.splitByCharset("Hello世界", false);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).value());
        assertTrue(result.get(0).isAscii());
        assertEquals("世界", result.get(1).value());
        assertFalse(result.get(1).isAscii());
    }

    @Test
    void splitByCharset_handlesMultipleSwitches() {
        List<StringSegment> result = StringUtils.splitByCharset("Hello世界World", false);
        assertEquals(3, result.size());
        assertEquals("Hello", result.get(0).value());
        assertEquals("世界", result.get(1).value());
        assertEquals("World", result.get(2).value());
    }

    // delimitByCharset tests
    @Test
    void delimitByCharset_joinsWithDelimiter() {
        String result = StringUtils.delimitByCharset("Hello世界", " / ");
        assertEquals("Hello / 世界", result);
    }

    // splitBySentences tests
    @Test
    void splitBySentences_splitsOnDelimiters() {
        // note: next char after delimiter is included in current sentence
        List<String> result = StringUtils.splitBySentences("Hello, World. Test");
        assertEquals(3, result.size());
        assertEquals("Hello, ", result.get(0));
        assertEquals("World. ", result.get(1));
        assertEquals("Test", result.get(2));
    }

    @Test
    void splitBySentences_handlesChinese() {
        // note: next char after delimiter is included in current sentence
        List<String> result = StringUtils.splitBySentences("你好，世界。测");
        assertEquals(2, result.size());
        assertEquals("你好，世", result.get(0));
        assertEquals("界。测", result.get(1));
    }

    // distributeTextToBlocks tests
    @Test
    void distributeTextToBlocks_splitsIntoBlocks() {
        String text = "First sentence, second sentence. Third.";
        List<String> result = StringUtils.distributeTextToBlocks(text, 20, 1);
        assertTrue(result.size() >= 2);
    }

    @Test
    void distributeTextToBlocks_keepsShortTextTogether() {
        String text = "Short.";
        List<String> result = StringUtils.distributeTextToBlocks(text, 20, 2);
        assertEquals(1, result.size());
    }

    // trim tests
    @Test
    void trim_trimsWhitespace() {
        assertEquals("hello", StringUtils.trim("  hello  "));
    }

    @Test
    void trim_returnsEmptyForNull() {
        assertEquals("", StringUtils.trim(null));
    }

    // normalizeListString tests
    @Test
    void normalizeListString_normalizesCommas() {
        assertEquals("a, b, c", StringUtils.normalizeListString("a,  b ,c"));
    }

    @Test
    void normalizeListString_compactMode() {
        assertEquals("a,b,c", StringUtils.normalizeListString("a,  b ,c", true));
    }

    @Test
    void normalizeListString_handlesNull() {
        assertEquals("", StringUtils.normalizeListString(null));
    }

    // split tests
    @Test
    void split_splitsAndTrims() {
        List<String> result = StringUtils.split("a, b,  c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void split_withCustomSeparator() {
        List<String> result = StringUtils.split("a; b; c", ";");
        assertEquals(List.of("a", "b", "c"), result);
    }

    // join tests
    @Test
    void join_joinsWithDefaultSeparator() {
        assertEquals("a, b, c", StringUtils.join(List.of("a", "b", "c")));
    }

    @Test
    void join_withCustomSeparator() {
        assertEquals("a-b-c", StringUtils.join(List.of("a", "b", "c"), "-"));
    }

    @Test
    void join_returnsEmptyForNull() {
        assertEquals("", StringUtils.join(null));
    }

    @Test
    void join_returnsEmptyForEmptyList() {
        assertEquals("", StringUtils.join(List.of()));
    }

    // sanitizeFilename tests
    @Test
    void sanitizeFilename_replacesPunctuation() {
        assertEquals("file_name_txt", StringUtils.sanitizeFilename("file:name.txt"));
    }
}

