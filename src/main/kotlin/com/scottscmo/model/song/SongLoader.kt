package com.scottscmo.model.song

import com.scottscmo.model.song.converters.KVMDConverter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object SongLoader {
    fun getSong(dataPath: String, titleSubstring: String): Song? {
        try {
            val songList = File(Path.of(dataPath).toString()).listFiles()
                ?.map { f -> f.name }
                ?.sorted()
                ?: emptyList()
            val title = songList
                .firstOrNull() { name -> name.lowercase(Locale.getDefault()).contains(titleSubstring) }
            val songFile = Files.readString(Path.of(dataPath, title))
            return KVMDConverter.parse(songFile)
        } catch (e: Exception) {
            throw Exception("Unable to load song by " + titleSubstring + ". " + e.message)
        }
    }
}
