import { parse } from "../parser";

describe("bible search text parser", () => {
    test("whole chapter and upper case", () => {
        expect(parse("Psalm 117")).toEqual({
            book: "psalm",
            ranges: [{
                chapter: 117,
                verses: []
            }]
        });
    });

    test("spaces", () => {
        expect(parse("song of songs 1 : 2 - 7 , 8 ; 3: 4 - 8 , 1 3 ")).toEqual({
            book: "song of songs",
            ranges: [{
                chapter: 1,
                verses: [2, 3, 4, 5, 6, 7, 8]
            }, {
                chapter: 3,
                verses: [4, 5, 6, 7, 8, 13]
            }]
        });
    });

    describe("one verse", () => {
        test("one chapter", () => {
            expect(parse("john 1:2")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2]
                }]
            });
        });
        test("multiple chapters", () => {
            expect(parse("john 1:2;3:4")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2]
                }, {
                    chapter: 3,
                    verses: [4]
                }]
            });
        });
    });

    describe("multiple verses", () => {
        test("one chapter", () => {
            expect(parse("john 1:2,3")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3]
                }]
            });
        });
        test("multiple chapters", () => {
            expect(parse("john 1:2,3;3:4,5")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2,3]
                }, {
                    chapter: 3,
                    verses: [4,5]
                }]
            });
        });
        test("verses not in order", () => {
            expect(parse("john 1:2,3,1")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 1]
                }]
            });
        });
    });

    describe("verse range", () => {
        test("one chapter", () => {
            expect(parse("john 1:2-5")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 4, 5]
                }]
            });
        });
        test("multiple chapters", () => {
            expect(parse("john 1:2-7;3:4-8")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 4, 5, 6, 7]
                }, {
                    chapter: 3,
                    verses: [4, 5, 6, 7, 8]
                }]
            });
        });
        test("reverse", () => {
            expect(() => parse("john 1:5-2")).toThrow(`Invalid verse range: "5-2". End verse should not be less than start verse.`);
        });
    });

    describe("multiple single verses and verse range", () => {
        test("one chapter", () => {
            expect(parse("john 1:2-5,8,9")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 4, 5, 8, 9]
                }]
            });
        });
        test("multiple chapters", () => {
            expect(parse("john 1:2-7,8;3:4-8,13")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 4, 5, 6, 7, 8]
                }, {
                    chapter: 3,
                    verses: [4, 5, 6, 7, 8, 13]
                }]
            });
        });

        test("repeat verses", () => {
            expect(parse("john 1:2-7,8,1-5")).toEqual({
                book: "john",
                ranges: [{
                    chapter: 1,
                    verses: [2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5]
                }]
            });
        });
    });
});
