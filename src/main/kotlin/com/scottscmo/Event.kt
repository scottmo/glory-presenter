package com.scottscmo

typealias EventHandler = (value: Any) -> Unit

object Event {
    const val DATA_DIR = "dataDir"

    private val handlers: MutableMap<String, MutableList<EventHandler>> = mutableMapOf()
    private val lastValues: MutableMap<String, Any> = mutableMapOf()

    fun emit(eventName: String, value: Any) {
        lastValues[eventName] = value
        handlers[eventName]?.forEach { it(value) }
    }

    fun subscribe(eventName: String, init: Boolean, handler: EventHandler) {
        val listeners = handlers.getOrDefault(eventName, mutableListOf())
        listeners.add(handler)
        handlers[eventName] = listeners

        if (init) {
            val value = lastValues[eventName]
            if (value != null) {
                handler(value)
            }
        }
    }

    fun subscribe(key: String, handler: EventHandler) {
        subscribe(key, false, handler)
    }
}
