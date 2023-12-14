// bible
export interface IBibleVerseText {
    bookIndex: string;
    chapter: number;
    verse: number;
    text: string;
}
export interface IBibleVerses {
    book: {
        [version: string]: string;
    };
    ranges: IBibleVerseRange[];
    verses: string[];
}
export interface IBibleVerseRange {
    chapter: number,
    verses?: number[],
}
export interface IBibleBookRange {
    book: string,
    ranges: IBibleVerseRange[],
}
export interface IBibleBookInfo {
    count: number[];
    index: number;
    name?: {
        [key: string]: string;
    };
}

// song
export interface IFormattedSong {
    id?: string;
    title?: string;
    lyrics?: string;
}

export interface ISong {
    title: string;
    collection?: string;
    verseOrder: number[];
    description?: string;
    lyrics: {
        verse: number;
        text: {
            [lang: string]: string[]
        }
    }[]
}

// slides
export interface ISlideTextConfig {
    alignment?: string;
    margin?: number;
    fontFamily?: string;
    fontSize?: number;
    fontColor?: string;
    fontStyles?: string;
    x?: number;
    y?: number;
}
export type ISlideConfig = {
    [langCode: string]: ISlideTextConfig & {
        numberOfCharactersPerLine?: number;
        numberOfLinesPerSlide?: number;
    }
}
