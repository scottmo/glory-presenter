package com.scottscmo.ppt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scottscmo.model.bible.BibleModel;
import com.scottscmo.model.bible.BibleReference;
import com.scottscmo.model.bible.BibleVerse;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;

public class BibleTemplateHandler implements TemplateHandler {
    private BibleModel bibleModel = BibleModel.getInstance();

    /**
     * expression format: e.g. {bible} cuv,niv - john 1:2-3;2:1-2
     */
    @Override
    public void evaluateTemplate(XMLSlideShow slides, int index) {
        XSLFSlide srcSlide = slides.getSlides().get(index);
        String bibleReference = findText(srcSlide, "{bible}");
        if (bibleReference != null) {
            insertBibleText(slides, index, bibleReference.substring(7).trim());
            System.out.println("Inserting bible text at " + srcSlide.getSlideNumber());
        }
    }

    public void insertBibleText(XMLSlideShow slides, int index, String bibleReferenceStr) {
        if (slides == null || bibleReferenceStr == null) return;
        insertBibleText(slides, index, new BibleReference(bibleReferenceStr));
    }

    public void insertBibleText(XMLSlideShow slides, int index, BibleReference ref) {
        if (slides == null || ref == null) return;

        Map<String, List<BibleVerse>> bibleVerses = bibleModel.getBibleVerses(ref);

        if (bibleVerses == null) return;

        XSLFSlideMaster masterSlide = slides.getSlideMasters().get(0);

        Map<String, String> bookNames = bibleModel.getBookNames(ref.getBook());

        // create title slide
        XSLFSlide titleSlide = slides.createSlide(masterSlide.getLayout("title"));
        for (String version : ref.getVersions()) {
            replaceText(titleSlide, "{title_" + version + "}", bookNames.get(version));
        }
        replaceText(titleSlide, "{range}", ref.getRanges().stream().map(range -> range.toString()).collect(Collectors.joining(";")));

        // create verse slides
        int numVerses = bibleVerses.get(ref.getVersions()[0]).size();
        for (int i = 0; i < numVerses; i++) {
            for (String version : ref.getVersions()) {
                XSLFSlide slide = slides.createSlide(masterSlide.getLayout("verse_" + version));
                BibleVerse verse = bibleVerses.get(version).get(i);
                String refStr = String.format("%s %d:%d", bookNames.get(version), verse.chapter(), verse.verse());

                replaceText(slide, "{verse}", verse.verse() + " " + verse.text());
                replaceText(slide, "{title}", refStr);
            }
        }
    }
}
