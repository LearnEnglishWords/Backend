package com.learnenglish.controllers

import com.learnenglish.models.BaseModel
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
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Validated
@Controller("/category")
@Secured(SecurityRule.IS_ANONYMOUS)
class CategoryController(
    private val wordService: WordService,
    private val categoryService: CategoryService
) : BaseController() {

    private val log = LoggerFactory.getLogger(WordService::class.java)


    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll(@QueryValue(defaultValue = "false") withWordsCount: Boolean): HttpResponse<Response> {
        val categories = categoryService.findAll()
        if (withWordsCount) {
            categories.forEach { category ->
                category.wordsCount = categoryService.getWordsCount(category.id!!)
            }
        }
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

        return HttpResponse.ok(Response(status = Status.OK.code, payload = mapOf("count" to wordList.size, "words" to wordList)))
    }


    @Post("/{id}/import")
    @Consumes(MediaType.TEXT_PLAIN)
    fun importWords(id: Long, @Body words: String): HttpResponse<Response> {
        val result: MutableList<BaseModel> = mutableListOf()
        val wordList = words.split("\n").filter { it.isNotEmpty() }.map { it.toLowerCase().trim() }

        val categoryError = Response(status = Status.INTERNAL_ERROR.code, payload = "Cannot find category with id: $id")
        val category = categoryService.findById(id) ?: return HttpResponse.serverError(categoryError)

        for (wordText in wordList) {
            val word = wordService.findByText(wordText)
            if(word == null) {
                val errorMessage = "Cannot find word: $wordText"
                println(errorMessage)
                result.add(ErrorState(message = errorMessage))
                continue
            }

            categoryService.addWord(categoryId = category.id!!, wordId = word.id!!)
            result.add(word)
        }

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = result)
        )
    }
}