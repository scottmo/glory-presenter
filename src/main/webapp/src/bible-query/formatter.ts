import { IBibleVerseRange } from "../types";

/**
 * Convert an array of verse numbers into a string representing the range of the verses
 * e.g. [1,2,3,5,10] => "1-3,5,10"
 */
function formatVerses(verseNumbers: number[]): string {
    const formatted = [];
    let verseRange = [verseNumbers[0]];
    for (let i = 1; i < verseNumbers.length; i++) {
        if (verseNumbers[i] - 1 !== verseNumbers[i - 1]) {
            formatted.push(verseRange.join("-"));
            verseRange = [verseNumbers[i]];
        } else {
            if (verseRange.length > 0) {
                verseRange[1] = verseNumbers[i];
            }
        }
    }
    formatted.push(verseRange.join("-"));
    return formatted.join(",");
}

export function format(book: string, ranges?: IBibleVerseRange[]): string {
    if (!book) {
        return "";
    }
    if (!ranges || ranges.length === 0) {
        return book;
    }
    const rangeStr = ranges.map(({ chapter, verses }) => {
        if (chapter === 0) {
            return "";
        }
        if (!verses || verses.length === 0) {
            return chapter;
        }
        return `${chapter}:${formatVerses(verses)}`;
    }).join(";");
    return `${book} ${rangeStr}`;
}
