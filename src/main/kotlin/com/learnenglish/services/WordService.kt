package com.learnenglish.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.learnenglish.config.DbConfig
import com.learnenglish.models.*
import it.skrape.core.htmlDocument
import it.skrape.exceptions.ElementNotFoundException
import it.skrape.extractIt
import it.skrape.selects.DocElement
import it.skrape.selects.and
import it.skrape.selects.eachText
import it.skrape.selects.html5.div
import it.skrape.selects.html5.li
import it.skrape.selects.html5.span
import it.skrape.skrape
import org.slf4j.LoggerFactory
import javax.inject.Singleton



fun List<String>.getToIndex(num: Int) = this.let {
    val size = if (it.size > num) num else it.size
    it.subList(0, size)
}

@Singleton
class WordService(
    private val categoryService: CategoryService,
    private val collectionService: CollectionService
) {
    private val db = DbConfig.getInstance()
    private val log = LoggerFactory.getLogger(WordService::class.java)

    fun create(word: Word): BaseModel? {
        try {
            db.withHandle<Long, Exception> {
                val mapper = ObjectMapper()
                it.createUpdate("insert into words (text, pronunciation, state, sense, examples) values(:text, :pronunciation, state, JSON_ARRAY(:sense), JSON_ARRAY(:examples))")
                    .bind("text", word.text)
                    .bind("pronunciation", mapper.writeValueAsString(word.pronunciation))
                    .bind("state", word.state)
                    .bind("sense", word.sense.joinToString("|"))
                    .bind("examples", word.examples.joinToString("|"))
                    .execute()
                    .toLong()
            }
            return word
        } catch (e: Exception) {
            return ErrorState(code = 0, type = "Unknown", message = "Some problem during saving into database.")
        }
    }

    fun update(word: Word): Boolean {
        val mapper = ObjectMapper()
        return try {
            db.withHandle<Long, Exception> {
                it.createUpdate("update words set text=:text, pronunciation=:pronunciation, state=:state, sense=JSON_ARRAY(:sense), examples=JSON_ARRAY(:examples) where text=:text")
                    .bind("text", word.text)
                    .bind("pronunciation", mapper.writeValueAsString(word.pronunciation))
                    .bind("state", word.state)
                    .bind("sense", word.sense.joinToString("|"))
                    .bind("examples", word.examples.joinToString("|"))
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

    fun getCount(state: WordState? = null): Int? {
        return try {
            db.withHandle<Int, Exception> {
                it.select("select count(*) from words ${if(state != null) "where state=:state" else ""}")
                    .apply { if (state != null) this.bind("state", state) }
                    .mapTo(Int::class.java)
                    .list().first()
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

    private fun List<DocElement>.getExamples(filter: Boolean): List<String>
        = eachText().getToIndex(10).apply {
            if (filter) return this.filter {
                if (it.contains("(=") && it.contains(")")) return@filter false
                if (it.contains("/")) return@filter false
                return@filter true
            }.getToIndex(5)
        }

    fun parse(wordText: String, filter: Boolean = true): Word {
        val pronunciation: MutableMap<String, String> = mutableMapOf()
        var wordTypes: List<String> = listOf()

        var word = skrape {
            url = "https://dictionary.cambridge.org/dictionary/english/$wordText"

            extractIt<Word> {
                htmlDocument {
                    try {
                        span {  withClass = "eg" and "deg"
                            it.examples = findAll { getExamples(filter) }
                        }
                    } catch (e: ElementNotFoundException) {
                        li {  withClass = "eg"
                            it.examples = findAll { getExamples(filter) }
                        }
                    }
                    try {
                        span {  withClass = "us" and "dpron-i"
                            htmlDocument(findFirst { html }) {
                                span { withClass = "ipa" and "dipa"
                                    pronunciation["us"] = findFirst { text }
                                }
                            }
                        }
                    } catch (e: ElementNotFoundException) {
                        log.warn("Cannot parse US pronunciation for word: ${wordText}.")
                    }
                    try {
                        span {
                            withClass = "uk" and "dpron-i"
                            htmlDocument(findFirst { html }) {
                                span {
                                    withClass = "ipa" and "dipa"
                                    pronunciation["uk"] = findFirst { text }
                                }
                            }
                        }
                    } catch (e: ElementNotFoundException) {
                        log.warn("Cannot parse UK pronunciation for word: ${wordText}.")
                    }
                    span {  withClass = "hw" and "dhw"
                        it.text = findFirst { text }
                    }
                    div {  withClass = "posgram" and "dpos-g"
                        wordTypes = findFirst { text }.split(", ").map { it.capitalize() }
                    }
                }
            }
        }

        if (pronunciation.isEmpty()) {
            log.error("Pronunciation is empty for word: ${word}")
            throw Exception("Pronunciation is empty for word: ${word}.")
        }

        word.pronunciation = pronunciation
        //word.sense = skrape {
        //    url = "https://glosbe.com/en/cs/$wordText"

        //    extract {
        //        htmlDocument {
        //            strong {  withClass = "phr"
        //                findAll {
        //                    eachText().getToIndex(10)
        //                }
        //            }
        //        }
        //    }
        //}
        //word.state = WordState.PARSE

        //if(findByText(wordText) != null ) {
        //    update(word)
        //} else {
        //    create(word)
        //}

        //word = findByText(word.text)!!
        try {
            addWordIntoDefaultCategories(word, wordTypes)
        } catch (e: Exception) {
            log.error("Cannot add word: ${word} into default categories.")
        }

        return word
    }

    private fun addWordIntoDefaultCategories(word: Word, categories: List<String>) {
        val wordCategories = findCategories(wordId = word.id!!)
        for (categoryName in categories) {
            if (!wordCategories.filter { categoryName == it.name }.isEmpty()) continue

            val basicCollection = collectionService.findByName(DefaultCollections.BASIC.value)!!
            val category = categoryService.findOrCreate(Category(name = categoryName, collectionId = basicCollection.id))!!
            categoryService.addWord(categoryId = category.id!!, wordId = word.id!!)
        }
    }
}