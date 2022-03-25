package com.scottscmo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Files
import java.nio.file.Path

object Config {
    const val SONG_YAML_DIR = "songs"
    const val SONG_SLIDES_DIR = "songs_slide"
    const val SONG_TEXT_DIR = "songs_txt"
    const val SONG_CSV_DIR = "songs_csv"

    const val GOOGLE_API_DIR = "google_api"
    const val GOOGLE_API_CREDENTIALS_PATH = "${GOOGLE_API_DIR}/client.info"

    private const val CONFIG_PATH = "./config.yaml"

    private lateinit var config: AppConfig

    fun get(): AppConfig = config

    fun load() {
        config = Files.newBufferedReader(Path.of(CONFIG_PATH)).use {
            ObjectMapper(YAMLFactory()).readValue(it, AppConfig::class.java)
        }
    }

    fun getRelativePath(fileName: String): String {
        return Path.of(config.dataDir, fileName).toFile().canonicalPath
    }
}

data class AppConfig(
    var dataDir: String,
    val clientInfoKey: String,
    val googleSlideConfig: SlideConfig,
) {
    // default config
    constructor(): this(
        "./data",
        "secretKey",
        SlideConfig(
            "PT",
            720.0,
            405.0,
            ParagraphConfig(
                "CENTER",
                -1.0,
                0.0,
                0.0
            ),
            "en",
            listOf("zh", "en"),
            mapOf(
                "cuv" to "zh",
                "niv" to "en"
            ),
            mapOf(
                "zh" to TextConfig(
                    "",
                    "STKaiti",
                    55.0,
                    "255, 255, 255",
                    "bold",
                    10,
                    4
                ),
                "en" to TextConfig(
                    " ",
                    "Arial Narrow",
                    40.0,
                    "255, 255, 153",
                    "bold",
                    30,
                    5
                )
            )
        )
    )
}

data class SlideConfig(
    val unit: String = "PT",
    val slideWidth: Double = 0.0,
    val slideHeight: Double = 0.0,
    val paragraph: ParagraphConfig = ParagraphConfig(),
    val defaultTextConfig: String = "",
    val textConfigsOrder: List<String> = emptyList(),
    val bibleVersionToTextConfig: Map<String, String> = emptyMap(),
    val textConfigs: Map<String, TextConfig> = emptyMap(),
)

data class ParagraphConfig(
    val alignment: String = "CENTER",
    val indentation: Double = 0.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
)

data class TextConfig(
    val wordDelimiter: String = "",
    val fontFamily: String = "",
    val fontSize: Double = 12.0,
    val fontColor: String = "0, 0, 0",
    val fontStyles: String = "",
    val numberOfCharactersPerLine: Int = 30,
    val numberOfLinesPerSlide: Int = 4,
)
