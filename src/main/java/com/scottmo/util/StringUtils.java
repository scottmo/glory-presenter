package com.scottmo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtils {
    private static final Pattern asciiRegex = Pattern.compile("^[\\x00-\\x7F]*$", Pattern.CASE_INSENSITIVE);
    public static boolean isASCII(String str) {
        return asciiRegex.matcher(str).find();
    }

    private static final Pattern punctuationDigitRegex = Pattern.compile("^[!-@\\[-`{-~]+$");
    public static boolean isPunctuationOrDigit(String str) {
        return punctuationDigitRegex.matcher(str).find();
    }

    public static List<StringSegment> splitByCharset(String str, boolean shouldMergeNumbers) {
        List<StringSegment> result = new ArrayList<>();

        StringSegment segment = null;
        for (int i = 0; i < str.length(); i++) {
            String ch = String.valueOf(str.charAt(i));
            boolean isAscii = isASCII(ch);
            if (segment != null) {
                if (segment.isAscii() == isAscii) {
                    segment.endIndex(segment.endIndex() + 1);
                    segment.value(segment.value() + ch);
                } else {
                    result.add(segment);
                    segment = null;
                }
            }
            if (segment == null) {
                segment = new StringSegment(i, i, ch, isAscii);
            }
        }
        if (segment != null && !segment.value().isEmpty()) {
            result.add(segment);
        }

        if (shouldMergeNumbers) {
            return mergeWithNeighbors(result, StringUtils::isPunctuationOrDigit,
                    (s1, s2) -> s1.isAscii() == s2.isAscii());
        }

        return result;
    }

    private static List<StringSegment> mergeWithNeighbors(List<StringSegment> stringSegments,
                                                          Function<String, Boolean> shouldProcess,
                                                          BiFunction<StringSegment, StringSegment, Boolean> shouldMerge) {
        List<StringSegment> result = new ArrayList<>();
        boolean merged = false;
        for (int i = 0; i < stringSegments.size(); i++) {
            if (merged) {
                merged = false;
                continue;
            }
            StringSegment segment = stringSegments.get(i);
            if (shouldProcess.apply(segment.value())) {
                StringSegment prevSegment = i == 0 ? null : stringSegments.get(i-1);
                StringSegment nextSegment = i == stringSegments.size() - 1 ? null : stringSegments.get(i + 1);
                if (prevSegment == null && nextSegment != null // start
                    && !shouldMerge.apply(segment, nextSegment)) {
                    // pass this segment's data to next, ignore current segment
                    nextSegment.value(segment.value() + nextSegment.value());
                    nextSegment.startIndex(segment.startIndex());
                } else if (nextSegment == null && prevSegment != null // end
                    && !shouldMerge.apply(segment, prevSegment)) {
                    // pass this segment's data to prev, ignore current segment
                    prevSegment.value(prevSegment.value() + segment.value());
                    prevSegment.endIndex(segment.endIndex());
                } else if (prevSegment != null && nextSegment != null // middle
                    && shouldMerge.apply(prevSegment, nextSegment)
                    && !shouldMerge.apply(prevSegment, segment)) {
                    // pass current segment and nextSegment's data to previous, ignore current and next
                    prevSegment.value(prevSegment.value() + segment.value() + nextSegment.value());
                    prevSegment.endIndex(nextSegment.endIndex());
                    merged = true;
                } else {
                    result.add(segment);
                }
            } else {
                result.add(segment);
            }
        }
        return result;
    }

    public static String delimitByCharset(String str, String delim) {
        return splitByCharset(str, true).stream()
                .map(segment -> segment.value().trim())
                .collect(Collectors.joining(delim));
    }

    public static List<String> splitBySentences(String str) {
        String sentenceDelimiters = ",.;，。；、:："; // both Chinese and English
        String quotes = "'\"‘“"; // both Chinese and English

        List<String> sentences = new ArrayList<>();
        String sentence = String.valueOf(str.charAt(0));
        for (int i = 1; i < str.length(); i++) {
            String ch = String.valueOf(str.charAt(i));
            String prevChar = String.valueOf(str.charAt(i - 1));
            sentence += ch;
            if (sentenceDelimiters.contains(prevChar)) {
                if (quotes.contains(ch)) {
                    sentence += ch;
                }
                sentences.add(sentence);
                sentence = "";
            }
        }
        // left over
        if (!sentence.isEmpty()) {
            sentences.add(sentence);
        }
        return sentences;
    }

    public static List<String> distributeTextToBlocks(String text, int charsPerLine,
            int linesPerBlock) {
        List<String> slideTexts = new ArrayList<>();

        List<String> sentences = splitBySentences(text);

        int charsPerSlide = charsPerLine * linesPerBlock;

        String currentSlideText = "";
        for (String sentence: sentences) {
            String newSlideText = currentSlideText + sentence;
            if (newSlideText.length() > charsPerSlide) {
                slideTexts.add(currentSlideText);
                currentSlideText = sentence;
            } else {
                currentSlideText = newSlideText;
            }
        }
        if (!currentSlideText.isEmpty()) {
            slideTexts.add(currentSlideText);
        }
        return slideTexts;
    }

    public static String trim(String str) {
        return str == null ? "" : str.trim();
    }

    public static String normalizeListString(String str) {
        return normalizeListString(str, false);
    }

    public static String normalizeListString(String str, boolean isCompact) {
        String sep = ",";
        String finalSep = isCompact ? sep : sep + " ";
        return str == null
                ? ""
                : Arrays.stream(str.split(sep)).map(String::trim).collect(Collectors.joining(finalSep));
    }

    public static List<String> split(String str) {
        return split(str, ",");
    }

    public static List<String> split(String str, String sep) {
        return Arrays.stream(str.split(sep)).map(String::trim).toList();
    }

    public static String sanitizeFilename(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
