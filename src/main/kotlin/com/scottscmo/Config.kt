package com.scottscmo

import java.nio.file.Path

typealias UpdateListener = (value: String) -> Unit

object Config {
    private val config: MutableMap<String, String> = HashMap()
    private val listenersMap: MutableMap<String, MutableList<UpdateListener>> = HashMap()
    const val DIR_DATA = "dirData"

    init {
        // default config
        config[DIR_DATA] = Path.of("./data").toAbsolutePath().toString()
    }

    operator fun get(key: String): String? {
        return config[key]
    }

    fun getOrDefault(key: String, defaultValue: String): String {
        return config.getOrDefault(key, defaultValue)
    }

    operator fun set(key: String, value: String) {
        config[key] = value
        listenersMap[key]?.forEach { it(value) }
    }

    fun subscribe(key: String, handler: UpdateListener) {
        val listeners = listenersMap.getOrDefault(key, ArrayList())
        listeners.add(handler)
        listenersMap[key] = listeners
    }
}