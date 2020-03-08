package com.learnenglish.models

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class Category(
    @get:NotBlank(message = "Name is required")
    @get:Size(min = 3, max = 50)
    var name: String = "",
    var collectionId: Long? = null
) : BaseModel(id = null)