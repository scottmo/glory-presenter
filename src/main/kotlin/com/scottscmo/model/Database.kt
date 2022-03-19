package com.scottscmo.model

import com.scottscmo.Config
import com.scottscmo.Event
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Database(private val dbName: String) {
    private var conn: Connection? = null

    init {
        Event.subscribe(Event.DATA_DIR) { _ -> conn = null }
    }

    private val dbPath: Path
        get() = Path.of(Config.getRelativePath("$dbName.db"))

    @Throws(SQLException::class)
    fun connect(): Connection? {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        }
        return conn
    }

    val isEmpty: Boolean
        get() {
            return !(Files.exists(dbPath) && dbPath.toFile().length() > 0)
        }
}