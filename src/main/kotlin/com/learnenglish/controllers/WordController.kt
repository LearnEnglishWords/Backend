package com.learnenglish.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.WordService
import com.learnenglish.models.Word
import com.learnenglish.models.ErrorState
import com.learnenglish.models.WordState
import javax.validation.Valid

@Validated
@Controller("/word")
@Secured(SecurityRule.IS_ANONYMOUS)
class WordController(private val wordService: WordService) : BaseController() {

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
        val word = wordService.parse(text, filter)

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = word)
        )
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
        val result: MutableList<Word> = mutableListOf()
        val wordsList = words.split("\n").filter { it.isNotEmpty() }

        for (word in wordsList) {
            if (wordService.findByText(word) == null) {
                result.add(wordService.create(Word(text = word, state = WordState.IMPORT)) as Word)
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

    @Get("/transform")
    fun transform(): HttpResponse<Response> {
        val words = wordService.findAll(0, 1000)!!

        for (word in words) {
            if (word.pronunciation["oldPronunciation"].isNullOrEmpty()) continue
            try {
                word.pronunciation = wordService.parse(word.text).pronunciation
                wordService.update(word)
                println("Updated: ${word.text}")
            } catch (e: Exception) {
                return HttpResponse.serverError(Response(status = Status.INTERNAL_ERROR.code, payload = "Error during transform: ${word.text}"))
            }
        }

        return HttpResponse.ok(Response(status = Status.OK.code, payload = "ok"))
    }
}