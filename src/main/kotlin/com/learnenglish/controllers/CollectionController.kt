package com.learnenglish.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.CollectionService
import com.learnenglish.models.Collection
import com.learnenglish.models.WordState
import com.learnenglish.services.CategoryService
import com.learnenglish.services.WordService
import io.micronaut.http.MutableHttpResponse
import javax.validation.Valid

@Validated
@Controller("/collection")
@Secured(SecurityRule.IS_ANONYMOUS)
class CollectionController(
    private val collectionService: CollectionService,
    private val categoryService: CategoryService,
    private val wordService: WordService
) : BaseController() {

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun getList(): MutableHttpResponse<Response>? {
        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = collectionService.findAll())
        )
    }

    @Get("/{id}/categories/")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCategories(id: Long): MutableHttpResponse<Response> {
        val words = categoryService.findAll(id)

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = words)
        )
    }

    @Get("/{id}/words/")
    @Produces(MediaType.APPLICATION_JSON)
    fun getWords(
        id: Long,
        @QueryValue(defaultValue = "false") shuffle: Boolean,
        @QueryValue(defaultValue = "CORRECT") state: WordState
    ): MutableHttpResponse<Response> {
        val words = wordService.findAllByCollection(id, state)

        return HttpResponse.ok(
            Response(status = Status.OK.code, payload = words?.toMutableList().apply {
                if (shuffle && !this.isNullOrEmpty()) {
                    this.shuffle()
                }
            })
        )
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(id: Long): HttpResponse<Collection> {
        val collection = collectionService.findById(id)
        return if (collection != null) {
            HttpResponse.ok(collection)
        } else HttpResponse.notFound()
    }

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun save(@Body @Valid collection: Collection): HttpResponse<Collection> {
        val collection = collectionService.create(collection)
        return if (collection != null) {
            HttpResponse.created(collection)
        } else HttpResponse.badRequest()
    }

    @Put("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun update(@Body @Valid collection: Collection): HttpResponse<Collection> {
        collectionService.update(collection)
        return if (collection != null) {
            HttpResponse.ok(collection)
        } else HttpResponse.badRequest()
    }

    @Delete("/{id}")
    fun remove(id: Long): HttpResponse<Any> {
        val collection = collectionService.findById(id) ?: return HttpResponse.notFound()

        collectionService.delete(collection.id!!)
        return HttpResponse.ok()
    }
}
