package com.scottscmo.bible;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BibleReference {
    private String book;
    private List<VerseRange> ranges;

    public String toString() {
        if (this.book == null || this.book.isEmpty()) {
            return "";
        }

        if (this.ranges == null || this.ranges.isEmpty()) {
            return this.book;
        }

        return String.format("%s %s",
            book,
            ranges.stream().map(VerseRange::toString).collect(Collectors.joining(";"))
        );
    }

    public static BibleReference of() {

        // const RE_VERSE_NOTATION = /([\d]?[A-z\s]+)\s+([\d,;:\-\s]+)/;
        // const RE_WHITESPACE = /\s+/g;
        
        // function parseVerseNotation(verseNotation: string) {
        //     const matches = verseNotation.match(RE_VERSE_NOTATION);
        //     if (matches && matches.length === 3) { // 3 = full string + book + ranges
        //         const book = (matches[1] as string)
        //                 .trim().replace(RE_WHITESPACE, " ").toLowerCase();
        //         const rangesStr = (matches[2] as string)
        //                 .trim().replace(RE_WHITESPACE, "");
        //         return { book, rangesStr };
        //     }
        //     return {};
        // }
        
        // function isVerseRange(verseRangeStr: string): boolean {
        //     return verseRangeStr.includes("-");
        // }
        
        // function parseVerseRange(verseRangeStr: string): number[] {
        //     const verseRange = [];
        
        //     const [ minVerse, maxVerse ] = verseRangeStr.split("-").map(Number);
        
        //     if (minVerse > maxVerse) {
        //         throw new Error(`Invalid verse range: "${verseRangeStr}". End verse should not be less than start verse.`);
        //     }
        //     for (let i = minVerse; i <= maxVerse; i++) {
        //         verseRange.push(i);
        //     }
        //     return verseRange;
        // }
        
        // // matthew 1:3-4,2
        // export function parse(verseNotation: string): Bible.BookRange {
        //     if (!verseNotation) {
        //         throw new Error("Missing verseNotation!");
        //     }
        
        //     const { book, rangesStr } = parseVerseNotation(verseNotation);
        
        //     if (!book || !rangesStr) {
        //         throw new Error("Missing book name or verse ranges!");
        //     }
        
        //     const ranges = rangesStr.split(";")
        //         .filter(Boolean)
        //         .map((range: string) => {
        //             const [ chapter, verses = "" ] = range.split(":");
        //             return {
        //                 chapter: Number(chapter),
        //                 verses: verses.split(",")
        //                     .filter(Boolean)
        //                     .map(verse => isVerseRange(verse)
        //                         ? parseVerseRange(verse)
        //                         : Number(verse))
        //                     .flat()
        //             };
        //         });
        
        //     return { book, ranges };
        // }
        
    }

    public record VerseRange(
        int chapter,
        int[] verses
    ) {
        public String toString() {
            if (this.chapter == 0) {
                return "";
            }
            if (this.verses == null || this.verses.length == 0) {
                return this.chapter + "";
            }

            List<String> verseRangeStrs = new ArrayList<>();
            int startVerse = this.verses[0];
            int endVerse = -1;
            for (int i = 1; i < this.verses.length; i++) {
                if (endVerse + 1 == this.verses[i]) { // consective, update endVerse to cover current verse
                    endVerse++;
                } else { // not consective
                    verseRangeStrs.add(formatVerseRange(startVerse, endVerse));
                    startVerse = this.verses[i];
                    endVerse = -1;
                }
            }
            verseRangeStrs.add(formatVerseRange(startVerse, endVerse));
            return verseRangeStrs.stream().collect(Collectors.joining(","));
        }

        private String formatVerseRange(int startVerse, int endVerse) {
            return endVerse <= startVerse
                ? startVerse + ""
                : startVerse + "-" + endVerse;
        }
    }
}
