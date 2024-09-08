package com.scottmo.core.bible.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.scottmo.core.Service;
import com.scottmo.core.bible.api.bibleMetadata.BibleVerse;
import com.scottmo.core.bible.api.bibleReference.BibleReference;

public interface BibleService extends Service {

    int insert(Map<String, List<List<String>>> bible, String version);

    Map<String, List<BibleVerse>> getBibleVerses(BibleReference ref);

    List<String> getAvailableVersions();

    Map<String, String> getBookNames(String bookId);

    void importOsisBible(File osisFile) throws IOException;

}
