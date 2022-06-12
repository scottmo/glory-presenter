package com.scottscmo.util

object KVMD {
    private const val KEY_NAMESPACE = "namespace"
    private const val KEY_METADATA = "metadata"
    private const val KEY_CONTENT = "content"

    private const val SECTION_DELIMITER = "==="

    private const val ERR_MSG_INVALID_FORMAT = "KVMD.parse: Invalid input format!"

    fun getNamespace(obj: Map<String, Any>): String {
        return obj[KEY_NAMESPACE] as String
    }

    fun getMetadata(obj: Map<String, Any>): Map<String, Any> {
        return (obj[KEY_METADATA] ?: mapOf<String, Any>()) as Map<String, Any>
    }

    fun getContent(obj: Map<String, Any>): Map<String, Any> {
        return (obj[KEY_CONTENT] ?: mapOf<String, Any>()) as Map<String, Any>
    }

    fun create(namespace: String, metadata: Map<String, Any>, content: Map<String, Any>): Map<String, Any> {
        return mapOf(KEY_NAMESPACE to namespace, KEY_METADATA to metadata, KEY_CONTENT to content)
    }

    fun parse(input: String): Map<String, Any> {
        val namespace: String
        val metadata: Map<String, Any>
        val content: Map<String, Any>

        val sections = input.split(SECTION_DELIMITER).map { it.trim() }
        assert(sections.size <= 3) { ERR_MSG_INVALID_FORMAT + " Too many sections" }

        when (sections.size) {
            1 -> { // content
                namespace = ""
                metadata = mapOf()
                content = parseContent(sections[0])
            }
            2 -> { // metadata, content
                namespace = ""
                metadata = parseMetadata(sections[0])
                content = parseContent(sections[1])
            }
            else -> { // name, metadata, content
                namespace = sections[0]
                metadata = parseMetadata(sections[1])
                content = parseContent(sections[2])
            }
        }

        return create(namespace, metadata, content)
    }

    private fun parseMetadata(input: String): Map<String, Any> {
        return input.split("\n")
            .filter { it.isNotEmpty() }
            .map { it.split(":") }
            .associate { it[0].trim() to parseMetadataValue(it[1].trim()) }
    }

    private fun parseMetadataValue(value: String): Any {
        if (value.startsWith("[") && value.endsWith("]")) { // array
            return value.substring(1, value.length - 1).split(",").map { it.trim() }
        }
        return value // string
    }

    /**
     * Recursively parse content. Throw if level is missing.
     */
    private fun parseContent(input: String, prefix: String = "#", level: Int = 1): Map<String, Any> {
        // make sure we have the prefix at the very least
        val keyRegex = "(^|\n)$prefix".toRegex()
        if (!keyRegex.containsMatchIn(input)) return mapOf()

        // find all the keys and recursively parse the values
        val currentLevelkeyRegex = "(^|\n)$prefix{$level}\\s(.+)($|\n)".toRegex()

        val keys = currentLevelkeyRegex.findAll(input).map { it.groupValues[2] }.toList()

        assert(keys.isNotEmpty()) { ERR_MSG_INVALID_FORMAT + " Missing level $level for prefix $prefix" }

        val values = input.split(currentLevelkeyRegex)
            .filter { it.isNotEmpty() }
            .map { it.trim() }
            .map { value -> parseContent(value, prefix, level + 1).ifEmpty { value } }
        return keys.zip(values).toMap()
    }

    /**
     * Serialize object to KVMD format. Should be in the following structure:
     * {
     *     metadata: {
     *         [key: String]: String | Array
     *     },
     *     content: {
     *         [key: String]: String | Map
     *     }
     * }
     */
    fun stringify(obj: Map<String, Any>): String {
        val namespace = getNamespace(obj)
        val metadataStr = getMetadata(obj).entries
                .sortedBy { it.key }
                .joinToString("\n") { it.key + ": " + stringifyMetadataValue(it.value) }
        val contentStr = stringifyContent(getContent(obj))

        return listOf(namespace, metadataStr, contentStr)
            .filter { it.isNotEmpty() }
            .joinToString("\n$SECTION_DELIMITER\n") + "\n"
    }

    private fun stringifyMetadataValue(value: Any): String {
        if (value is List<*>) {
            return "[" + value.joinToString(", ") + "]"
        }
        return value.toString()
    }

    private fun stringifyContent(content: Map<String, Any>, prefix: String = "#", level: Int = 1): String {
        return content.entries
            .sortedBy { it.key }
            .joinToString("\n") {
                val key = "\n" + prefix.repeat(level) + " " + it.key
                if (it.value is Map<*, *>) {
                    key + "\n" + stringifyContent(it.value as Map<String, Any>, prefix, level + 1)
                } else {
                    key + "\n" + it.value
                }
            }
    }

    private fun testParser() {
        val parsed = parse("""
獻上感恩 Give Thanks
===
tags: Henry Smith
order: [v1, v2, v1, v2]
===

# v1

## zh
献上感恩的心
归给至圣全能神

## en
Give thanks with a grateful heart
give thanks to the holy One.

# v2
## zh
如今软弱者已得刚强
贫穷者已成富足
## en
And now let the weak say I am strong
let the poor say I am rich
        """.trimIndent())
        println(parsed)
        val parsedStr = stringify(parsed)
        println(parsedStr)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        testParser()
    }
}