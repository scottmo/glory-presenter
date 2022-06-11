package com.scottscmo.util

object KVMD {
    private const val SECTION_DELIMITER = "==="

    private const val ERR_MSG_INVALID_FORMAT = "KVMD.parse: Invalid input format!"
    
    fun parse(input: String): Map<String, Any> {
        val metadata: Map<String, Any>
        val content: Map<String, Any>

        val sections = input.split(SECTION_DELIMITER).map { it.trim() }
        assert(sections.size <= 3) { ERR_MSG_INVALID_FORMAT + " Too many sections" }

        when (sections.size) {
            1 -> { // content
                metadata = mapOf()
                content = parseContent(sections[0])
            }
            2 -> { // metadata, content
                metadata = parseMetadata(sections[0])
                content = parseContent(sections[1])
            }
            else -> { // name, metadata, content
                metadata = mapOf("name" to sections[0]) + parseMetadata(sections[1])
                content = parseContent(sections[2])
            }
        }

        return mapOf("metadata" to metadata, "content" to content)
    }

    private fun parseMetadata(input: String): Map<String, Any> {
        return input.split("\n")
            .filter { it.isNotEmpty() }
            .map { it.split(":") }
            .associate { it[0].trim() to parseMetadataValue(it[1].trim()) }
    }

    private fun parseMetadataValue(value: String): Any {
        if (value.startsWith("[") && value.endsWith("]")) { // array
            return value.substring(1, value.length - 1).split(",")
        }
        return value // string
    }

    /**
     * Recursively parse content. Throw if level is missing.
     */
    private fun parseContent(input: String, prefix: String = "#", level: Int = 1): Map<String, Any> {
        // make sure we have the prefix at the very least
        val keyRegex = "(^|\n)$prefix".toRegex()
        if (!keyRegex.matches(input)) return mapOf()

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

    fun stringify(obj: Map<String, Map<String, Any>>): String {
        return ""
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(parse("""
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
        """.trimIndent()))
    }
}