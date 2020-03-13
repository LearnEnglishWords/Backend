package com.learnenglish.controllers

import com.learnenglish.models.ErrorState
import com.learnenglish.models.Import
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.ImportService
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import javax.validation.Valid

@Validated
@Controller("/import")
@Secured(SecurityRule.IS_ANONYMOUS)
class ImportController(private val importService: ImportService) : BaseController() {


    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(@Body @Valid import: Import): HttpResponse<Response> {
        return if (import.words.isNotEmpty()) {
            for (word in import.words) {
                importService.importWord(word)
            }
            HttpResponse.ok(
                Response(status = Status.OK.code, payload = "Created: ${import.words}")
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code, error = ErrorState(message = "List of words is empty."))
        )
    }
}