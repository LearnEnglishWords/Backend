package com.learnenglish.models

import org.jdbi.v3.core.mapper.reflect.ColumnName
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class Category(
    @get:NotBlank(message = "Name is required")
    @get:Size(min = 3, max = 50)
    var name: String = "",
    @ColumnName("name_cs")
    var czechName: String = "",
    var collectionId: Long? = null,
    var wordsCount: Int? = null
) : BaseModel(id = null) {

    companion object {
        fun parse(map: Map<String, Any?>): Category {
            return Category().apply {
                id = (map["id"] as Int).toLong()
                name = map["name"] as String
                collectionId = (map["collection_id"] as Int?)?.toLong()
            }
        }
    }
}