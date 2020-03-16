package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.*
import it.skrape.core.htmlDocument
import it.skrape.extract
import it.skrape.extractIt
import it.skrape.selects.and
import it.skrape.selects.eachText
import it.skrape.selects.html5.span
import it.skrape.selects.html5.strong
import it.skrape.skrape
import javax.inject.Singleton


fun List<String>.getToIndex(num: Int) = this.let {
    val size = if (it.size > num) num else it.size
    it.subList(0, size)
}

@Singleton
class WordService {
    private val db = DbConfig.getInstance()

    fun create(word: Word): BaseModel? {
        try {
            db.withHandle<Long, Exception> {
                it.createUpdate("insert into words (text, pronunciation, state, sense, examples) values(:text, :pronunciation, state, JSON_ARRAY(:sense), JSON_ARRAY(:examples))")
                    .bind("text", word.text)
                    .bind("pronunciation", word.pronunciation)
                    .bind("state", word.state)
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
                it.createUpdate("update words set text=:text, pronunciation=:pronunciation, state=:state, sense=JSON_ARRAY(:sense), examples=JSON_ARRAY(:examples) where text=:text")
                    .bind("text", word.text)
                    .bind("pronunciation", word.pronunciation)
                    .bind("state", word.state)
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

    fun findAll(offset: Int, limit: Int, state: WordState? = null): List<Word>? {
        return try {
            db.withHandle<List<Word>, Exception> {
                it.select("select * from words ${if(state != null) "where state=:state" else ""} limit :offset, :limit")
                    .bind("offset", offset)
                    .bind("limit", limit)
                    .apply { if (state != null) this.bind("state", state) }
                    .mapToMap()
                    .list()
                    .map { Word.parse(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun findCategories(wordId: Long): List<Category> {
        return try {
            db.withHandle<List<Category>, Exception> {
                it.select("""
                    select * from categories_words cw 
                    join categories c on cw.category_id=c.id 
                    where cw.word_id=:wordId
                """)
                    .bind("wordId", wordId)
                    .mapToMap()
                    .list()
                    .map { Category.parse(it) }
            }
        } catch (e: Exception) {
            listOf()
        }
    }

    fun findByCategory(categoryId: Long): List<Word> {
        return try {
            db.withHandle<List<Word>, Exception> {
                it.select("""
                    select * from categories_words cw 
                    join words w on cw.word_id=w.id 
                    where cw.category_id=:categoryId
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

    fun findByText(wordText: String): Word? {
        return try {
            db.withHandle<Word, Exception> {
                it.select("select * from words where text=:wordText")
                        .bind("wordText", wordText)
                        .mapToMap()
                        .list()
                        .firstOrNull()
                        ?.let { Word.parse(it) }
            }
        } catch (e: Exception) {
            null
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

    fun parse(wordText: String): Word {
        val word = skrape {
            url = "https://dictionary.cambridge.org/dictionary/english/$wordText"

            extractIt<Word> {
                htmlDocument {
                    span {  withClass = "eg" and "deg"
                        findAll {
                            it.examples = eachText().getToIndex(10)
                        }
                    }
                    span {  withClass = "ipa" and "dipa" and "lpr-2" and "lpl-1"
                        it.pronunciation = findFirst { text }
                    }
                    span {  withClass = "hw" and "dhw"
                        it.text = findFirst { text }
                    }
                }
            }
        }
        word.sense = skrape {
            url = "https://glosbe.com/en/cs/$wordText"

            extract {
                htmlDocument {
                    strong {  withClass = "phr"
                        findAll {
                            eachText().getToIndex(10)
                        }
                    }
                }
            }
        }
        word.state = WordState.PARSE
        return word
    }
}