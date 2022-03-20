package com.scottscmo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Files
import java.nio.file.Path

object Config {
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
    var dataDir: String = "./data",
    val clientInfoKey: String = "secretKey",
    val bibleVersionToLanguage: Map<String, List<String>>,
    val googleSlideConfig: SlideConfig,
)

data class SlideConfig(
    val unit: String = "PT",
    val slideWidth: Double = 720.0,
    val slideHeight: Double = 405.0,
    val paragraph: ParagraphConfig,
    val text: Map<String, TextConfig>,
)

data class ParagraphConfig(
    val alignment: String = "CENTER",
    val indentation: Double = -1.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
)

data class TextConfig(
    val delimiter: String = "",
    val fontFamily: String = "",
    val fontSize: Double = 50.0,
    val fontColor: String = "",
    val fontStyles: String = "",
    val numberOfCharactersPerLine: Int = 10,
    val numberOfLinesPerSlide: Int = 5,
)
