package com.learnenglish.controllers

import com.learnenglish.models.Category
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.CollectionService
import com.learnenglish.models.Collection
import com.learnenglish.services.CategoryService
import javax.validation.Valid

@Validated
@Controller("/collections")
@Secured(SecurityRule.IS_ANONYMOUS)
class CollectionController(
    private val collectionService: CollectionService,
    private val categoryService: CategoryService
) {

    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun getList(): HttpResponse<List<Collection>> {
        return HttpResponse.ok(collectionService.findAll())
    }

    @Get("/{id}/categories/")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCategories(id: Long): HttpResponse<List<Category>> {
        return HttpResponse.ok(categoryService.findAllByCollection(id))
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
        val collection = collectionService.save(collection)
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