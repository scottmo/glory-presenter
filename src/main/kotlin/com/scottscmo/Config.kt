package com.scottscmo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Files
import java.nio.file.Path

object Config {
    const val SONG_YAML_DIR = "songs"
    const val SONG_TEXT_DIR = "songs_txt"
    const val SONG_CSV_DIR = "songs_csv"

    const val GOOGLE_API_DIR = "google_api"
    const val GOOGLE_API_CREDENTIALS_PATH = "${Config.GOOGLE_API_DIR}/client.info"

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
    var clientInfoKey: String,
    var bibleVersionToLanguage: Map<String, String>,
    var googleSlideConfig: SlideConfig,
) {
    // default config
    constructor(): this(
        "./data",
        "secretKey",
        mapOf(
            "cuv" to "zh",
            "niv" to "en"
        ),
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
    val unit: String,
    val slideWidth: Double,
    val slideHeight: Double,
    val paragraph: ParagraphConfig,
    val text: Map<String, TextConfig>,
)

data class ParagraphConfig(
    val alignment: String,
    val indentation: Double,
    val x: Double,
    val y: Double,
)

data class TextConfig(
    val delimiter: String,
    val fontFamily: String,
    val fontSize: Double,
    val fontColor: String,
    val fontStyles: String,
    val numberOfCharactersPerLine: Int,
    val numberOfLinesPerSlide: Int,
)
