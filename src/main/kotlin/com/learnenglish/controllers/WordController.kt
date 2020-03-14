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
import javax.validation.Valid

@Validated
@Controller("/word")
@Secured(SecurityRule.IS_ANONYMOUS)
class WordController(private val wordService: WordService) : BaseController() {

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): HttpResponse<Response> {
        val words = wordService.findAll()
        return HttpResponse.ok(Response(status = Status.OK.code, payload = words))
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

    @Get("/parse/{wordText}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun parse(wordText: String): HttpResponse<Response> {
        if(!"[a-z]\\w+".toRegex().matches(wordText)) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "You can use only [a-z] characters."))
        )
        val word = wordService.parse(wordText)
        wordService.create(word)

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = word)
        )
    }

    @Get("/find/{wordText}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun find(wordText: String): HttpResponse<Response> {
        if(!"[a-z]\\w+".toRegex().matches(wordText)) return HttpResponse.badRequest(
            Response( status = Status.BAD_REQUEST.code, error = ErrorState(message = "You can use only [a-z] characters."))
        )

        val word = wordService.findByText(wordText) ?: wordService.parse(wordText)

        return HttpResponse.ok(
                Response(status = Status.OK.code, payload = word)
        )
    }

    @Post("/import")
    @Consumes(MediaType.TEXT_PLAIN)
    fun importWords(@Body words: String): HttpResponse<Response> {
        val result: MutableList<Word> = mutableListOf()
        val wordsList = words.split("\n")

        for (word in wordsList) {
            result.add(wordService.create(Word(text = word)) as Word)
        }

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = result)
        )
    }

}