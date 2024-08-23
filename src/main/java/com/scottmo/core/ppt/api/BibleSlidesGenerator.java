package com.scottmo.core.ppt.api;

import java.io.IOException;

public interface BibleSlidesGenerator {

    // TODO: determine hasStartSlide and hasEndSlide from templatefile
    void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException;

    void generate(String bibleRefString, String tmplFilePath, String outputFilePath,
            boolean hasStartSlide, boolean hasEndSlide) throws IOException;
}
