package com.scottscmo.ppt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scottscmo.model.bible.BibleModel;
import com.scottscmo.model.bible.BibleReference;
import com.scottscmo.model.bible.BibleVerse;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;

public class BibleTemplateHandler implements TemplateHandler {
    private final BibleModel bibleModel = BibleModel.getInstance();

    /**
     * expression format: e.g. {bible} cuv,niv - john 1:2-3;2:1-2
     */
    @Override
    public void evaluateTemplate(XMLSlideShow ppt, int index) {
        XSLFSlide srcSlide = ppt.getSlides().get(index);
        String bibleReference = findText(srcSlide, "{bible}");
        if (bibleReference != null) {
            insertBibleText(ppt, index, bibleReference.substring(7).trim());
            System.out.println("Inserting bible text at " + srcSlide.getSlideNumber());
        }
    }

    public void insertBibleText(XMLSlideShow ppt, int index, String bibleReferenceStr) {
        if (ppt == null || bibleReferenceStr == null) return;
        insertBibleText(ppt, index, new BibleReference(bibleReferenceStr));
    }

    public void insertBibleText(XMLSlideShow ppt, int index, BibleReference ref) {
        if (ppt == null || ref == null) return;

        Map<String, List<BibleVerse>> bibleVerses = bibleModel.getBibleVerses(ref);

        if (bibleVerses == null) return;

        Map<String, XSLFSlideLayout> layouts = new HashMap<>();
        layouts.put("title", getSlideMaster(ppt, "title").getLayout("Title Slide"));
        for (String version : ref.getVersions()) {
            String key = "verse_" + version;
            XSLFSlideMaster slideMaster = getSlideMaster(ppt, key);
            layouts.put(key, slideMaster.getLayout("Title Slide"));
        }

        Map<String, String> bookNames = bibleModel.getBookNames(ref.getBook());

        // create title slide
        XSLFSlide titleSlide = ppt.createSlide(layouts.get("title"));
        Map<String, String> titleSlideValues = new HashMap<>();
        for (String version : ref.getVersions()) {
            titleSlideValues.put("{title_" + version + "}", bookNames.get(version));
        }
        titleSlideValues.put("{range}", ref.getRanges().stream()
                .map(BibleReference.VerseRange::toString).collect(Collectors.joining(";")));
        replaceText(titleSlide, titleSlideValues);

        // create verse slides
        int numVerses = bibleVerses.get(ref.getVersions()[0]).size();
        for (int i = 0; i < numVerses; i++) {
            for (String version : ref.getVersions()) {
                XSLFSlide slide = ppt.createSlide(layouts.get("verse_" + version));
                BibleVerse verse = bibleVerses.get(version).get(i);
                String refStr = String.format("%s %d:%d", bookNames.get(version), verse.chapter(), verse.verse());

                replaceText(slide, Map.of(
                        "{verse}", verse.verse() + " " + verse.text(),
                        "{title}", refStr
                ));
            }
        }
    }
}
