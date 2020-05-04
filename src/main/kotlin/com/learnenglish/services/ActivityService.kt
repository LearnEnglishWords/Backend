package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Activity
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton

interface ActivityDao {
    @SqlUpdate("insert into activities (uuid, timestamp) values(:uuid, :timestamp)")
    @GetGeneratedKeys
    fun save(@BindBean activity: Activity): Long?

    @SqlQuery("select timestamp from activities where uuid=:uuid")
    fun getByUUID(@Bind("uuid") uuid: String): List<Activity>
}

@Singleton
class ActivityService() {
    private val db = DbConfig.getInstance()

    fun list(): List<Map<String, String>>? {
        return try {
            db.withHandle<List<Map<String, String>>, Exception> {
                it.select("select uuid, count(timestamp) as count from activities group by uuid")
                    .mapToMap()
                    .list() as List<Map<String, String>>?
            }
        } catch (x: Exception) {
            null
        }
    }

    fun getByUUID(uuid: String): List<Activity>? {
        return try {
            db.onDemand<ActivityDao>().getByUUID(uuid)
        } catch (x: Exception) {
            null
        }
    }

    fun save(activity: Activity): Boolean {
        return try {
            db.onDemand<ActivityDao>().save(activity)
            true
        } catch (x: Exception) {
            println(x.message)
            false
        }
    }
}