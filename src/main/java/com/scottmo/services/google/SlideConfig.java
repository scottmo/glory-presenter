package com.scottmo.services.google;

import java.util.List;
import java.util.Map;

public record SlideConfig(
        String unit,
        Double slideWidth,
        Double slideHeight,
        ParagraphConfig paragraph,
        String defaultTextConfig,
        String defaultAsciiTextConfig,
        String defaultNonAsciiTextConfig,
        List<String> textConfigsOrder,
        Map<String, String> bibleVersionToTextConfig,
        Map<String, TextConfig> textConfigs
) {}
