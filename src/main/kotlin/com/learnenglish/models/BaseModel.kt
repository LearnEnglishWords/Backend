package com.learnenglish.models

open class ErrorState(val code: Int?, val type: String?, val message: String?) : BaseModel(id = null)

open class BaseModel(
    var id: Long? = null
)
