package com.learnenglish.controllers

import com.learnenglish.models.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.validation.Validated
import com.learnenglish.services.ActivityService
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid

@Validated
@Controller("/activity")
@Secured(SecurityRule.IS_ANONYMOUS)
class ActivityController(private val activityService: ActivityService) : BaseController() {

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(@Body @Valid activity: Activity): MutableHttpResponse<Response>? {
        if (activity.uuid == null) {
            return HttpResponse.badRequest(
                Response(status = Status.BAD_REQUEST.code)
            )
        }
        val result = activityService.save(activity)
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
    fun list(): MutableHttpResponse<Response>? {
        val result =  activityService.list()
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
        val result = activityService.getByUUID(uuid)
        return if (result != null) {
            HttpResponse.created(
                Response(status = Status.OK.code, payload = result)
            )
        } else HttpResponse.badRequest(
            Response(status = Status.BAD_REQUEST.code)
        )
    }
}
