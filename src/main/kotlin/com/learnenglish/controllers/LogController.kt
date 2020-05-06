package com.learnenglish.controllers

import com.learnenglish.models.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.validation.Validated
import com.learnenglish.services.LogService
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@Controller("/log")
@Secured(SecurityRule.IS_ANONYMOUS)
class LogController(private val logService: LogService) : BaseController() {

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(@Body @Valid log: Log): MutableHttpResponse<Response>? {
        val result = logService.save(log)
        return if (result) {
            HttpResponse.created(
                Response(status = Status.CREATED.code)
            )
        } else HttpResponse.serverError(
            Response(status = Status.INTERNAL_ERROR.code)
        )
    }

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun list(@QueryValue from: String?): MutableHttpResponse<Response>? {
        val result = logService.getAll(if (from.isNullOrEmpty()) null else LocalDateTime.parse(from))
        return if (result != null) {
            HttpResponse.created(
                Response(status = Status.OK.code, payload = result)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code)
        )
    }

    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@QueryValue uuid: String): MutableHttpResponse<Response>? {
        val result = logService.getByUUID(uuid)
        return if (result != null) {
            HttpResponse.created(
                Response(status = Status.OK.code, payload = result)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code)
        )
    }
}