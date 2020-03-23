package com.learnenglish.models

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

enum class DefaultCollections(val value: String) {
    BASIC("Basic"),
    ADVANCED("Advanced"),
    INTERMEDIATE("Intermediate")
}

data class Collection(
    var id: Long? = null,
    @get:NotBlank(message = "Name is mandatory")
    @get:Size(min = 3, max = 50)
    var name: String = ""
)