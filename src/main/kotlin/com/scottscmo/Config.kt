package com.scottscmo

import java.nio.file.Path

typealias UpdateListener = (value: String) -> Unit

object Config {
    private val config: MutableMap<String, String> = mutableMapOf()
    private val listenersMap: MutableMap<String, MutableList<UpdateListener>> = mutableMapOf()
    const val DIR_DATA = "dirData"

    init {
        // default config
        config[DIR_DATA] = Path.of("./data").toAbsolutePath().toString()
    }

    operator fun get(key: String): String {
        return config[key] ?: ""
    }

    operator fun set(key: String, value: String) {
        config[key] = value
        listenersMap[key]?.forEach { it(value) }
    }

    fun getRelativePath(fileName: String): String {
        return Path.of(config[DIR_DATA] ?: "./data", fileName).toString()
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