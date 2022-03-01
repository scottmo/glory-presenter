package com.scottscmo

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

typealias UpdateListener = (value: String) -> Unit

object Config {
    const val DATA_DIR = "dataDir"
    const val CLIENT_INFO_KEY = "clientInfoKey"

    private const val DEFAULT_DATA_DIR = "./data"
    private const val CONFIG_PATH = "./config.yaml"

    private val config: MutableMap<String, String> = try {
            val mapper = ObjectMapper(YAMLFactory())
            val content = Files.readString(Path.of(CONFIG_PATH), StandardCharsets.UTF_8)
            val mapType = mapper.typeFactory.constructMapType(MutableMap::class.java, String::class.java, String::class.java)
            mapper.readValue(content, mapType)
        } catch (e: JsonProcessingException) {
            getDefaultConfig()
        } catch (e: IOException) {
            getDefaultConfig()
        }

    private val listenersMap: MutableMap<String, MutableList<UpdateListener>> = mutableMapOf()

    private fun getDefaultConfig() = mutableMapOf(
        DATA_DIR to Path.of(DEFAULT_DATA_DIR).toFile().canonicalPath
    )

    operator fun get(key: String): String {
        return config[key] ?: ""
    }

    operator fun set(key: String, value: String) {
        config[key] = value
        listenersMap[key]?.forEach { it(value) }
    }

    fun getRelativePath(fileName: String): String {
        return Path.of(config[DATA_DIR] ?: DEFAULT_DATA_DIR, fileName).toFile().canonicalPath
    }

    fun subscribe(key: String, init: Boolean, handler: UpdateListener) {
        val listeners = listenersMap.getOrDefault(key, mutableListOf())
        listeners.add(handler)
        listenersMap[key] = listeners

        if (init) {
            val value = this[key]
            if (value.isNotEmpty()) {
                handler(value)
            }
        }
    }

    fun subscribe(key: String, handler: UpdateListener) {
        subscribe(key, false, handler)
    }
}