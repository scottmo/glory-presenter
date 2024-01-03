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
export interface SlideTextConfig {
    alignment?: string;
    margin?: number;
    fontFamily?: string;
    fontSize?: number;
    fontColor?: string;
    fontStyles?: string;
    x?: number;
    y?: number;
}
export type SlideConfig = {
    [langCode: string]: SlideTextConfig & {
        numberOfCharactersPerLine?: number;
        numberOfLinesPerSlide?: number;
    }
}

export type SongVerse = {
    name: string;
    text: string;
    locale: string;
};
export type Song = {
    id?: string;
    authors?: string[];
    publisher?: string;
    copyright?: string;
    songBook?: string;
    entry?: string;
    comments?: string;
    titles?: {
        text: string;
        locale: string;
    }[];
    verseOrder?: string[];
    verses?: SongVerse[];
}
