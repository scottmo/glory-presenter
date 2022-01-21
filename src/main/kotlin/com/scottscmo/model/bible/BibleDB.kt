package com.scottscmo.model.bible

import com.scottscmo.model.Database
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

internal object BibleDB {
    private val db = Database("bible")

    @Throws(SQLException::class)
    fun connect(): Connection? {
        return db.connect()
    }

    fun useStatement(run: (stmt: Statement) -> Unit) {
        var stmt: Statement? = null
        try {
            stmt = connect()!!.createStatement()
            run(stmt)
        } finally {
            stmt?.close()
        }
    }

    fun usePrepareStatement(sql: String, run: (stmt: PreparedStatement) -> Unit) {
        var stmt: PreparedStatement? = null
        try {
            stmt = connect()!!.prepareStatement(sql)
            run(stmt)
        } finally {
            stmt?.close()
        }
    }

    fun isEmpty(): Boolean {
        return db.isEmpty
    }
}