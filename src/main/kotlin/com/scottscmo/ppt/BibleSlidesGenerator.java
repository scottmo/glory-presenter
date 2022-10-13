package com.scottscmo.ppt;

import com.scottscmo.Config;
import com.scottscmo.bibleMetadata.BibleMetadata;
import com.scottscmo.model.bible.BibleModel;
import com.scottscmo.bibleReference.BibleReference;
import com.scottscmo.model.bible.BibleVerse;
import com.scottscmo.bibleMetadata.BookMetadata;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BibleSlidesGenerator {
    private static final String TITLE_MASTER_KEY = "title";
    private static final String VERSE_MASTER_KEY_PREFIX = "verse";
    private static final String MAIN_LAYOUT_KEY = "main";

    private static final BibleModel bibleModel = BibleModel.get();
    
    private static void insertBibleText(String templateFilePath, String outputFilePath, String bibleReference) throws IOException {
        try (var inStream = new FileInputStream(templateFilePath)) {
            var ppt = new XMLSlideShow(inStream);
            insertBibleText(ppt, bibleReference);
            ppt.removeSlide(0);
            try (var outStream = new FileOutputStream(outputFilePath)) {
                ppt.write(outStream);
            }
            ppt.close();
        }
    }

    public static void generate(String templatePath, String destDir, String versions, String verses) throws IOException {
        if (verses.isEmpty()) {
            for (var entry : BibleMetadata.getBookInfoMap().entrySet()) {
                String bookName = entry.getKey();
                BookMetadata bookMetadata = entry.getValue();
                for (int i = 0; i < bookMetadata.count().size(); i++) {
                    String chapter = bookName + " " + (i + 1);
                    insertBibleText(Config.getRelativePath(templatePath),
                        Config.getRelativePath("%s/%s.pptx".formatted(destDir, chapter)), "%s - %s".formatted(versions, chapter));
                }
            }
        } else {
            String bibleReference = "%s - %s".formatted(versions, verses);
            insertBibleText(Config.getRelativePath(templatePath),
                Config.getRelativePath("%s/%s.pptx".formatted(destDir, bibleReference)), bibleReference);
        }
    }

    public static void generate(String templatePath, String destDir, String versions) throws IOException {
        generate(templatePath, destDir, versions, "");
    }

    private static void insertBibleText(XMLSlideShow ppt, String bibleReferenceStr) {
        insertBibleText(ppt, new BibleReference(bibleReferenceStr));
    }

    private static void insertBibleText(XMLSlideShow ppt, BibleReference ref) {
        Map<String, List<BibleVerse>> bibleVerses = bibleModel.getBibleVerses(ref);
        Map<String, String> bookNames = bibleModel.getBookNames(ref.getBook());
        assert bookNames != null : "Unable to query book names with book ${ref.book}";

        // create title slide
        var titleLayout = TemplatingUtil.getSlideMasterLayout(ppt, TITLE_MASTER_KEY, MAIN_LAYOUT_KEY);
        assert titleLayout != null : "Missing title master slide";

        var titleSlide = ppt.createSlide(titleLayout);
        Map<String, String> titleSlideValues = new HashMap<>();
        for (String version : ref.getVersions()) {
            titleSlideValues.put("{title_%s}".formatted(version), bookNames.getOrDefault(version, ""));
        }
        titleSlideValues.put("{range}", ref.getRangesString());
        TemplatingUtil.replacePlaceholders(titleSlide, titleSlideValues);

        // create verse slides
        Map<String, XSLFSlideLayout> verseLayouts = new HashMap<>();
        for (String version : ref.getVersions()) {
            String key = "%s_%s".formatted(VERSE_MASTER_KEY_PREFIX, version);
            verseLayouts.put(key, TemplatingUtil.getSlideMasterLayout(ppt, key, MAIN_LAYOUT_KEY));
        }

        int numVerses = bibleVerses.get(ref.getVersions().get(0)).size();
        for (int i = 0; i < numVerses; i++) {
            for (String version : ref.getVersions()) {
                var slide = ppt.createSlide(verseLayouts.get("%s_%s".formatted(VERSE_MASTER_KEY_PREFIX, version)));
                BibleVerse verse = bibleVerses.get(version).get(i);
                String refStr = String.format("%s %d:%d", bookNames.get(version), verse.chapter(), verse.index());
                TemplatingUtil.replacePlaceholders(slide, Map.of(
                    "{verse}", verse.index() + " " + verse.text(),
                    "{title}", refStr
                ));
            }
        }
    }
}
