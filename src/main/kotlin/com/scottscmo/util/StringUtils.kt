package com.scottscmo.util

object StringUtils {
    private val asciiRegex = Regex("^[\\x00-\\x7F]*$")
    fun isASCII(str: String): Boolean {
        return asciiRegex.matches(str)
    }

    private val punctuationDigitRegex = Regex("^[!-@\\[-`{-~]+$")
    fun isPunctuationOrDigit(str: String): Boolean {
        return punctuationDigitRegex.matches(str)
    }

    class StringSegment(var startIndex: Int, var endIndex: Int,
            var value: String, val isAscii: Boolean) {}
    fun splitByCharset(str: String, shouldMergeNumbers: Boolean): List<StringSegment> {
        val result = mutableListOf<StringSegment>()

        var segment: StringSegment? = null
        for (i in str.indices) {
            val char = str[i].toString()
            val isAscii = isASCII(char)
             if (segment != null) {
                if (segment.isAscii == isAscii) {
                    segment.endIndex++
                    segment.value += char
                } else {
                    result.add(segment)
                    segment = null
                }
            }
            if (segment == null) {
                segment = StringSegment(i, i, char, isAscii)
            }
        }
        if (segment != null && segment.value.isNotEmpty()) {
            result.add(segment)
        }

        if (shouldMergeNumbers) {
            return mergeWithNeighbors(result, { s -> isPunctuationOrDigit(s) },
                { s1, s2 -> s1.isAscii == s2.isAscii })
        }

        return result
    }

    private fun mergeWithNeighbors(stringSegments: List<StringSegment>,
            shouldProcess: (str: String) -> Boolean,
            shouldMerge: (s1: StringSegment, s2: StringSegment) -> Boolean): List<StringSegment> {
        val result = mutableListOf<StringSegment>();
        var merged = false
        for (i in stringSegments.indices) {
            if (merged) {
                merged = false
                continue
            }
            val segment = stringSegments[i];
            if (shouldProcess(segment.value)) {
                val prevSegment = if (i == 0) null else stringSegments[i-1];
                val nextSegment = if (i == stringSegments.size - 1) null else stringSegments[i+1];
                if (prevSegment == null && nextSegment != null // start
                    && !shouldMerge(segment, nextSegment)) {
                    // pass this segment's data to next, ignore current segment
                    nextSegment.value = segment.value + nextSegment.value;
                    nextSegment.startIndex = segment.startIndex;
                } else if (nextSegment == null && prevSegment != null // end
                    && !shouldMerge(segment, prevSegment)) {
                    // pass this segment's data to prev, ignore current segment
                    prevSegment.value = prevSegment.value + segment.value;
                    prevSegment.endIndex = segment.endIndex;
                } else if (prevSegment != null && nextSegment != null // middle
                    && shouldMerge(prevSegment, nextSegment)
                    && !shouldMerge(prevSegment, segment)) {
                    // pass current segment and nextSegment's data to previous, ignore current and next
                    prevSegment.value = prevSegment.value + segment.value + nextSegment.value;
                    prevSegment.endIndex = nextSegment.endIndex;
                    merged = true
                } else {
                    result.add(segment);
                }
            } else {
                result.add(segment);
            }
        }
        return result;
    }

    fun delimitByCharset(str: String, delim: String): String {
        return splitByCharset(str, true).joinToString(delim) { it.value.trim() }
    }

    fun splitBySentences(str: String): List<String> {
        val sentenceDelimiters = ",.;，。；、:："; // both Chinese and English
        val quotes = "'\"‘“"; // both Chinese and English

        val sentences = mutableListOf<String>();
        var sentence = str[0].toString();
        for (i in 1 until str.length) {
            val char = str[i];
            val prevChar = str[i-1];
            sentence += char;
            if (sentenceDelimiters.contains(prevChar)) {
                if (quotes.contains(char)) {
                    sentence += char;
                }
                sentences.add(sentence);
                sentence = "";
            }
        }
        // left over
        if (sentence.isNotEmpty()) {
            sentences.add(sentence);
        }
        return sentences;
    }

    fun distributeTextToBlocks(text: String, charsPerLine: Int,
        linesPerBlock: Int): List<String> {
        val slideTexts = mutableListOf<String>()

        val sentences = StringUtils.splitBySentences(text)

        val charsPerSlide = charsPerLine * linesPerBlock

        var currentSlideText = ""
        sentences.forEach { sentence ->
            val newSlideText = currentSlideText + sentence
            if (newSlideText.length > charsPerSlide) {
                slideTexts.add(currentSlideText)
                currentSlideText = sentence
            } else {
                currentSlideText = newSlideText
            }
        }
        if (currentSlideText.isNotEmpty()) {
            slideTexts.add(currentSlideText)
        }
        return slideTexts
    }
}
