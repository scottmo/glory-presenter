package com.scottmo.core.ppt.api;

import java.io.IOException;

import com.scottmo.core.Service;

public interface BibleSlidesGenerator extends Service {

    // TODO: determine hasStartSlide and hasEndSlide from templatefile
    void generate(String bibleRefString, String tmplFilePath, String outputFilePath) throws IOException;

    void generate(String bibleRefString, String tmplFilePath, String outputFilePath,
            boolean hasStartSlide, boolean hasEndSlide) throws IOException;
}
