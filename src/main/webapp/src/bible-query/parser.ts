import { IBibleBookRange } from "../types";

const RE_VERSE_NOTATION = /([\d]?[A-z\s]+)\s+([\d,;:\-\s]+)/;
const RE_WHITESPACE = /\s+/g;

function parseVerseNotation(verseNotation: string) {
    const matches = verseNotation.match(RE_VERSE_NOTATION);
    if (matches && matches.length === 3) { // 3 = full string + book + ranges
        const book = (matches[1] as string)
                .trim().replace(RE_WHITESPACE, " ").toLowerCase();
        const rangesStr = (matches[2] as string)
                .trim().replace(RE_WHITESPACE, "");
        return { book, rangesStr };
    }
    return {};
}

function isVerseRange(verseRangeStr: string): boolean {
    return verseRangeStr.includes("-");
}

function parseVerseRange(verseRangeStr: string): number[] {
    const verseRange = [];

    const [ minVerse, maxVerse ] = verseRangeStr.split("-").map(Number);

    if (minVerse > maxVerse) {
        throw new Error(`Invalid verse range: "${verseRangeStr}". End verse should not be less than start verse.`);
    }
    for (let i = minVerse; i <= maxVerse; i++) {
        verseRange.push(i);
    }
    return verseRange;
}

// matthew 1:3-4,2
export function parse(verseNotation: string): IBibleBookRange {
    if (!verseNotation) {
        throw new Error("Missing verseNotation!");
    }

    const { book, rangesStr } = parseVerseNotation(verseNotation);

    if (!book || !rangesStr) {
        throw new Error("Missing book name or verse ranges!");
    }

    const ranges = rangesStr.split(";")
        .filter(Boolean)
        .map((range: string) => {
            const [ chapter, verses = "" ] = range.split(":");
            return {
                chapter: Number(chapter),
                verses: verses.split(",")
                    .filter(Boolean)
                    .map(verse => isVerseRange(verse)
                        ? parseVerseRange(verse)
                        : Number(verse))
                    .flat()
            };
        });

    return { book, ranges };
}
