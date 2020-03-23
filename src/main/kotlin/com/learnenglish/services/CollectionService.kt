package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Collection
import com.learnenglish.models.DefaultCollections
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

    @SqlQuery("select * from collections where name=:name")
    fun findByName(@Bind("name") name: String): Collection?

    @SqlUpdate("delete from collections where id=:id")
    fun remove(@Bind("id") id: Long)
}

@Singleton
class CollectionService {
    private val db = DbConfig.getInstance()

    init {
        createDefaultCollections()
    }

    fun create(collection: Collection): Collection? {
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

    fun findByName(name: String): Collection? {
        return try {
            db.onDemand<CollectionDao>().findByName(name)
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

    fun createDefaultCollections(): Boolean {
        try {
            for (collection in DefaultCollections.values()) {
                if (findByName(collection.value) == null) {
                    create(Collection(name = collection.value))
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}