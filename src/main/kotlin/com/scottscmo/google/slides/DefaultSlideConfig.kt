package com.scottscmo.google.slides

object DefaultSlideConfig {
    const val ID_SHAPE_PREFIX = "o"
    const val ID_SLIDE_PREFIX = "s"
    const val ID_PLACEHOLDER_PREFIX = "p"

    const val SLIDE_H = 405
    const val SLIDE_W = 720
    const val SLIDE_BASE = 236.25

    const val SLIDE_DELIMITER = "\n\n---\n\n"

    const val FOOTER_TITLE_SIZE = 12
    const val FOOTER_TITLE_Y = 380

    val WORD_DELIMITER = mapOf<String, String>(
        "en" to " ",
        "zh" to ""
    )
}
