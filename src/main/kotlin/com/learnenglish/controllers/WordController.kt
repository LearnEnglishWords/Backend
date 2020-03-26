package com.learnenglish.controllers

import com.learnenglish.models.*
import com.learnenglish.services.CategoryService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.WordService
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Validated
@Controller("/word")
@Secured(SecurityRule.IS_ANONYMOUS)
class WordController(private val wordService: WordService) : BaseController() {

    private val log = LoggerFactory.getLogger(WordService::class.java)

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(
        @QueryValue(defaultValue = "1") page: Int,
        @QueryValue(defaultValue = "10") limit: Int,
        @QueryValue state: WordState?
    ): HttpResponse<Response> {
        if (page <= 0) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "Param page must be positive."))
        )
        if (limit <= 0) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "Param limit must be positive."))
        )
        val words = wordService.findAll((page -1) * limit, limit, state)
        val count = wordService.getCount(state)
        return HttpResponse.ok(Response(status = Status.OK.code, payload = mapOf("count" to count, "words" to words)))
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(id: Long): HttpResponse<Response> {
        val word = wordService.findById(id) ?: return HttpResponse.notFound()
        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = word)
        )
    }

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(@Body @Valid word: Word): HttpResponse<Response> {
        val word = wordService.create(word)
        return if (word is Word) {
            HttpResponse.created(
                Response(status = Status.OK.code, payload = word)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code, error = word as ErrorState)
        )
    }

    @Put("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun update(@Body @Valid word: Word): HttpResponse<Response> {
        wordService.findById(word.id!!) ?: return HttpResponse.notFound()

        if (wordService.update(word))
            return HttpResponse.ok()
        else
            return HttpResponse.badRequest()
    }

    @Delete("/{id}")
    fun remove(id: Long): HttpResponse<Any> {
        val word = wordService.findById(id) ?: return HttpResponse.notFound()

        wordService.delete(word.id!!)
        return HttpResponse.ok()
    }

    @Get("/parse")
    @Consumes(MediaType.APPLICATION_JSON)
    fun parse(@QueryValue text: String, @QueryValue(defaultValue = "true") filter: Boolean): HttpResponse<Response> {
        if(!"([a-z,A-Z])\\w+|([a-z,A-Z])".toRegex().matches(text)) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "You can use only [a-z,A-Z] characters."))
        )
        val word: Word
        try {
            word = wordService.parse(text, filter)
            return HttpResponse.ok(Response(status = Status.OK.code, payload = word))
        } catch (e: Exception) {
            log.error("Cannot parse word: $text")
            return HttpResponse.serverError(
                Response(
                    status = Status.INTERNAL_ERROR.code,
                    error = ErrorState(
                        code = 500,
                        type = "PARSE_ERROR",
                        message = "Cannot parse word: $text"
                    )
                )
            )
        }

    }

    @Get("/find")
    @Consumes(MediaType.APPLICATION_JSON)
    fun find(@QueryValue text: String): HttpResponse<Response> {
        if(!"([a-z,A-Z])\\w+|([a-z,A-Z])".toRegex().matches(text)) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "You can use only [a-z,A-Z] characters."))
        )

        val word = wordService.findByText(text) ?: wordService.parse(text)

        return HttpResponse.ok(
                Response(status = Status.OK.code, payload = word)
        )
    }

    @Post("/import")
    @Consumes(MediaType.TEXT_PLAIN)
    fun importWords(@Body words: String): HttpResponse<Response> {
        val result: MutableList<BaseModel> = mutableListOf()
        val wordLines = words.split("\n").filter { it.isNotEmpty() }
        val regex = """([a-z,A-Z]\w*);(\d+);([a-z])""".toRegex()

        for (wordLine in wordLines) {
            var (word, rank, wordTypeShortcut) = regex.find(wordLine)!!.destructured
            word = word.capitalize()

            val wordType = WordType.values().firstOrNull { it.shortcut == wordTypeShortcut }
            if (wordType == null) {
                result.add(ErrorState(message = "Wrong wordType shortcut for word: $word."))
                continue
            }

            var savedWord = wordService.findByText(word)
            if (savedWord == null) {
                savedWord = wordService.create(Word(text = word, rank = rank.toLong(), state = WordState.IMPORT)) as Word
                wordService.addIntoWordTypeCategory(savedWord, wordType)
                        ?: result.add(ErrorState(message = "Error cannot add into wordType: $wordType category word: $word.")) && continue
                result.add(savedWord)
            } else {
                savedWord.rank = rank.toLong()
                if (wordService.update(savedWord)) {
                    wordService.addIntoWordTypeCategory(savedWord, wordType)
                            ?: result.add(ErrorState(message = "Error cannot add into wordType: $wordType category word: $word.")) && continue
                    result.add(savedWord)
                } else {
                    result.add(ErrorState(message = "Error during update for word: $word."))
                }
            }
        }

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = result)
        )
    }

    @Get("/{id}/categories")
    fun getCatagories(id: Long): HttpResponse<Response> {
        val word = wordService.findById(id)
                ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find word with id: $id")))
        val wordList = wordService.findCategories(word.id!!)

        return HttpResponse.ok(Response(status = Status.OK.code, payload = wordList))
    }
}