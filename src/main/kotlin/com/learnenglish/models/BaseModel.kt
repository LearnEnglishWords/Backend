package com.learnenglish.models

open class ErrorState(val code: Int? = null, val type: String? = null, val message: String? = null) : BaseModel(id = null)

open class BaseModel(
    var id: Long? = null
)
