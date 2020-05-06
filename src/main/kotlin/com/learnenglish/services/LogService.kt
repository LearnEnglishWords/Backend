package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Log
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.time.LocalDateTime
import javax.inject.Singleton

interface LogDao {
    @SqlUpdate("insert into logs (uuid, timestamp, message) values(:uuid, :timestamp, :message)")
    @GetGeneratedKeys
    fun save(@BindBean log: Log): Long?

    @SqlQuery("select * from logs")
    fun getAll(): List<Log>

    @SqlQuery("select * from logs where timestamp > :from")
    fun getByTimestamp(@Bind("from") from: LocalDateTime): List<Log>

    @SqlQuery("select timestamp, message from logs where uuid=:uuid")
    fun getByUUID(@Bind("uuid") uuid: String): List<Log>
}

@Singleton
class LogService {
    private val db = DbConfig.getInstance()

    fun getAll(from: LocalDateTime?): List<Log>? {
        return try {
            if (from == null) {
                db.onDemand<LogDao>().getAll()
            } else {
                db.onDemand<LogDao>().getByTimestamp(from)
            }
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