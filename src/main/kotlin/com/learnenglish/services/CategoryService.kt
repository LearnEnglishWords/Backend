package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.BaseModel
import com.learnenglish.models.Category
import com.learnenglish.models.ErrorState
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.sql.SQLIntegrityConstraintViolationException
import javax.inject.Singleton

interface CategoryDao {
    @SqlUpdate("insert into categories (name, collection_id) values(:name, :collectionId)")
    @GetGeneratedKeys
    fun insert(@BindBean category: Category): Long

    @SqlUpdate("update categories set name=:name where id=:id")
    fun update(@BindBean category: Category): Int

    @SqlQuery("select * from categories")
    @RegisterBeanMapper(Category::class)
    fun findAll(): List<Category>

    @SqlQuery("select * from categories where collection_id=:collectionId")
    @RegisterBeanMapper(Category::class)
    fun findAllByCollection(@Bind("collectionId") collectionId: Long): List<Category>

    @SqlQuery("select * from categories where id=:id")
    fun findById(@Bind("id") id: Long): Category?

    @SqlUpdate("delete from categories where id=:id")
    fun remove(@Bind("id") id: Long)

    //@SqlQuery("select * from categories_words where wordId=:wordId and categoryId=:categoryId")
    //fun containWord(@Bind("categoryId") categoryId: Long, @Bind("wordId") wordId: Long)

    @SqlQuery("select count(*) from categories_words where category_id=:categoryId")
    fun getWordsCount(@Bind("categoryId") categoryId: Long): Int

    @SqlUpdate("insert into categories_words (word_id, category_id) values(:wordId, :categoryId)")
    fun addWord(@Bind("categoryId") categoryId: Long, @Bind("wordId") wordId: Long): Int

    @SqlUpdate("delete from categories_words where word_id=:wordId and category_id=:categoryId")
    fun removeWord(@Bind("categoryId") categoryId: Long, @Bind("wordId") wordId: Long): Int
}

@Singleton
class CategoryService {
    private val db = DbConfig.getInstance()

    fun create(category: Category): BaseModel? {
        try {
            category.id = db.onDemand<CategoryDao>().insert(category)
            return category
        } catch (e: UnableToExecuteStatementException) {
            if (e.cause is SQLIntegrityConstraintViolationException) {
                return ErrorState(
                        code = (e.cause as SQLIntegrityConstraintViolationException).errorCode,
                        type = "SQLIntegrityConstraintViolationException",
                        message = "Cannot save this category because collection with id: '${category.collectionId}' not exists."
                )
            } else {
                return ErrorState(code = 0, type = "Unknown", message = "Some problem during saving into database.")
            }
        }
    }

    fun update(category: Category): Boolean {
        return try {
            db.onDemand<CategoryDao>().update(category)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAll(): List<Category> {
        return db.onDemand<CategoryDao>().findAll()
    }

    fun findAllByCollection(collectionId: Long): List<Category> {
        return db.onDemand<CategoryDao>().findAllByCollection(collectionId)
    }

    fun findById(id: Long): Category? {
        return try {
            db.onDemand<CategoryDao>().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun containWord(categoryId: Long, wordId: Long): Boolean {
        //db.onDemand<CategoryDao>().containWord(categoryId, wordId)
        return db.withHandle<Boolean, Exception> {
            it.select("select * from categories_words where word_id=:wordId and category_id=:categoryId")
                .bind("categoryId", categoryId)
                .bind("wordId", wordId)
                .mapToMap()
                .list()
                .size > 0
        }
    }

    fun getWordsCount(categoryId: Long): Int? {
        return try {
            db.onDemand<CategoryDao>().getWordsCount(categoryId)
        } catch (e: Exception) {
            null
        }
    }

    fun delete(id: Long): Boolean {
        return try {
            db.onDemand<CategoryDao>().remove(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addWord(categoryId: Long, wordId: Long): Boolean {
        return try {
            db.onDemand<CategoryDao>().addWord(categoryId, wordId)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeWord(categoryId: Long, wordId: Long): Boolean {
        return try {
            db.onDemand<CategoryDao>().removeWord(categoryId, wordId)
            true
        } catch (e: Exception) {
            false
        }
    }
}