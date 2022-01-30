package com.scottscmo.model.song.adapters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.scottscmo.Config
import com.scottscmo.model.song.Song
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object SongYAMLAdapter {
    private val mapper = ObjectMapper(YAMLFactory())
    fun deserialize(serializedSong: String?): Song? {
        if (serializedSong == null) return null

        return try {
            mapper.readValue(serializedSong, Song::class.java)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            null
        }
    }

    fun getSong(songName: String): Song? {
        return deserialize(getSerializedSong(songName))
    }

    fun getSerializedSong(songName: String): String? {
        return try {
            val songPath = Path.of(Config.getRelativePath("songs/$songName.yaml"))
            Files.readString(songPath, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            System.err.println(e.message)
            null
        }
    }
}