package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Collection
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton

interface CollectionDao {
    @SqlUpdate("insert into collections (name) values(:name)")
    @GetGeneratedKeys
    fun save(@BindBean collection: Collection): Long

    @SqlUpdate("update collections set name=:name where id=:id")
    @GetGeneratedKeys
    fun update(@BindBean collection: Collection): Long

    @SqlQuery("select * from collections")
    @RegisterBeanMapper(Collection::class)
    fun findAll(): List<Collection>

    @SqlQuery("select * from collections where id=:id")
    fun findById(@Bind("id") id: Long): Collection?

    @SqlUpdate("delete from collections where id=:id")
    fun remove(@Bind("id") id: Long)
}

@Singleton
class CollectionService {
    private val db = DbConfig.getInstance()

    fun save(collection: Collection): Collection? {
        return try {
            collection.id = db.onDemand<CollectionDao>().save(collection)
            collection
        } catch (e: Exception) {
            null
        }
    }

    fun update(collection: Collection): Collection? {
        return try {
            db.onDemand<CollectionDao>().update(collection)
            collection
        } catch (e: Exception) {
            null
        }
    }

    fun findAll(): List<Collection> {
        return db.onDemand<CollectionDao>().findAll()
    }

    fun findById(id: Long): Collection? {
        return try {
            db.onDemand<CollectionDao>().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun delete(id: Long): Boolean {
        return try {
            db.onDemand<CollectionDao>().remove(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}