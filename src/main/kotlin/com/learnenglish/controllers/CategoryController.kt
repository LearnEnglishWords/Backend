package com.learnenglish.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.CategoryService
import com.learnenglish.models.Category
import com.learnenglish.models.ErrorState
import com.learnenglish.services.WordService
import javax.validation.Valid

@Validated
@Controller("/category")
@Secured(SecurityRule.IS_ANONYMOUS)
class CategoryController(
    private val wordService: WordService,
    private val categoryService: CategoryService
) : BaseController() {

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(): HttpResponse<Response> {
        val categories = categoryService.findAll()
        return HttpResponse.ok(Response(status = Status.OK.code, payload = categories))
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(id: Long): HttpResponse<Response> {
        val category = categoryService.findById(id)
        return if (category is Category) {
            HttpResponse.ok(
                Response(status = Status.OK.code, payload = category)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code, error = category as ErrorState)
        )
    }

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(@Body @Valid category: Category): HttpResponse<Response> {
        val category = categoryService.create(category)
        return if (category is Category) {
            HttpResponse.created(
                Response(status = Status.OK.code, payload = category)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code, error = category as ErrorState)
        )
    }

    @Put("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun update(@Body @Valid category: Category): HttpResponse<Response> {
        categoryService.findById(category.id!!) ?: return HttpResponse.notFound()

        if (categoryService.update(category))
            return HttpResponse.ok()
        else
            return HttpResponse.badRequest()
    }

    @Delete("/{id}")
    fun remove(id: Long): HttpResponse<Any> {
        val category = categoryService.findById(id) ?: return HttpResponse.notFound()

        categoryService.delete(category.id!!)
        return HttpResponse.ok()
    }

    @Post("/{id}/word/{wordId}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun addWord(id: Long, wordId: Long): HttpResponse<Response> {
        val category = categoryService.findById(id)
            ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find category with id: $id")))
        val word = wordService.findById(wordId)
            ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find word with id: $wordId")))
        val containWord = categoryService.containWord(categoryId = category.id!!, wordId = word.id!!)
        if (containWord) {
            return HttpResponse.badRequest(Response(
                status = Status.BAD_REQUEST.code,
                error = ErrorState(
                    message = "Word with id: $wordId is already in collection with id: $id"
                )
            ))
        }

        val response = categoryService.addWord(categoryId = category.id!!, wordId = word.id!!)
        return if (response) {
            HttpResponse.created(Response(status = Status.OK.code))
        } else HttpResponse.badRequest()
    }

    @Delete("/{id}/word/{wordId}")
    fun removeWord(id: Long, wordId: Long): HttpResponse<Response> {
        val category = categoryService.findById(id)
            ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find category with id: $id")))
        val word = wordService.findById(wordId)
            ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find word with id: $wordId")))

        val response = categoryService.removeWord(categoryId = category.id!!, wordId = word.id!!)
        return if (response) HttpResponse.ok()
        else HttpResponse.badRequest()
    }

    @Get("/{id}/words")
    fun getCatagoryWords(id: Long): HttpResponse<Response> {
        val category = categoryService.findById(id)
            ?: return HttpResponse.notFound(Response(status = Status.NOT_FOUND.code, error = ErrorState(message = "Cannot find category with id: $id")))
        val wordList = wordService.findByCategory(categoryId = category.id!!)

        return HttpResponse.ok(Response(status = Status.OK.code, payload = wordList))
    }
}