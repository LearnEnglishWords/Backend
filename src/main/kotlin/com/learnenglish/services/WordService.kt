package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.BaseModel
import com.learnenglish.models.Word
import com.learnenglish.models.ErrorState
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.BindKotlin
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton


@Singleton
class WordService {
    private val db = DbConfig.getInstance()

    fun create(word: Word): BaseModel? {
        try {
            word.id = db.withHandle<Long, Exception> {
                it.createUpdate("insert into words (text, pronunciation, sense, examples) values(:text, :pronunciation, JSON_ARRAY(:sense), JSON_ARRAY(:examples))")
                    .bind("text", word.text)
                    .bind("pronunciation", word.pronunciation)
                    .bind("sense", word.sense.joinToString())
                    .bind("examples", word.examples.joinToString())
                    .execute()
                    .toLong()
            }
            return word
        } catch (e: Exception) {
            return ErrorState(code = 0, type = "Unknown", message = "Some problem during saving into database.")
        }
    }

    fun update(word: Word): Boolean {
        return try {
            db.withHandle<Long, Exception> {
                it.createUpdate("update words set text=:text, pronunciation=:pronunciation, sense=JSON_ARRAY(:sense), examples=JSON_ARRAY(:examples) where id=:id")
                    .bind("id", word.id)
                    .bind("text", word.text)
                    .bind("pronunciation", word.pronunciation)
                    .bind("sense", word.sense.joinToString())
                    .bind("examples", word.examples.joinToString())
                    .execute()
                    .toLong()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAll(): List<Word>? {
        return try {
            db.withHandle<List<Word>, Exception> {
                it.select("select * from words")
                    .mapToMap()
                    .list()
                    .map { Word.parse(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun findByCategory(categoryId: Long): List<Word> {
        return try {
            db.withHandle<List<Word>, Exception> {
                it.select("""
                    select * from categories_words cw 
                    join words w on cw.wordId=w.id 
                    where cw.categoryId=:categoryId
                """)
                .bind("categoryId", categoryId)
                .mapToMap()
                .list()
                .map { Word.parse(it) }
            }
        } catch (e: Exception) {
            listOf()
        }
    }

    fun findById(id: Long): Word? {
        return try {
            db.withHandle<Word, Exception> {
                it.select("select * from words where id=:id")
                    .bind("id", id)
                    .mapToMap()
                    .list()
                    .first()
                    .let { Word.parse(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun delete(id: Long): Boolean {
        return try {
            db.withHandle<Int, Exception> {
                it.createUpdate("delete from words where id=:id")
                    .bind("id", id)
                    .execute()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}