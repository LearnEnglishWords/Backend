package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Log
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton

interface LogDao {
    @SqlUpdate("insert into logs (uuid, timestamp, message) values(:uuid, :timestamp, :message)")
    @GetGeneratedKeys
    fun save(@BindBean log: Log): Long?

    @SqlQuery("select * from logs")
    fun getAll(): List<Log>

    @SqlQuery("select timestamp, message from logs where uuid=:uuid")
    fun getByUUID(@Bind("uuid") uuid: String): List<Log>
}

@Singleton
class LogService() {
    private val db = DbConfig.getInstance()

    fun getAll(): List<Log>? {
        return try {
            db.onDemand<LogDao>().getAll()
        } catch (x: Exception) {
            null
        }
    }

    fun getByUUID(uuid: String): List<Log>? {
        return try {
            db.onDemand<LogDao>().getByUUID(uuid)
        } catch (x: Exception) {
            null
        }
    }

    fun save(log: Log): Boolean {
        return try {
            db.onDemand<LogDao>().save(log)
            true
        } catch (x: Exception) {
            println(x.message)
            false
        }
    }
}