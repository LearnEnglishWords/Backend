package com.learnenglish.controllers

import com.learnenglish.models.BaseModel
import com.learnenglish.models.ErrorState

abstract class BaseController {

    data class Response(val status: Int, val error: ErrorState? = null, val payload: Any? = null)

    enum class Status(val code: Int) {
        OK(200),
        CREATED(201),
        BAD_REQUEST(400),
        NOT_FOUND(404),
        INTERNAL_ERROR(500)
    }
}